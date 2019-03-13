package com.pedro.rtsp.rtsp;

import android.media.MediaCodec;
import android.util.Log;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;
import com.pedro.rtsp.utils.CreateSSLSocket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pedro on 10/02/17.
 */

public class RtspClient implements SendListener {

  private final String TAG = "RtspClient";
  private static final Pattern rtspUrlPattern =
      Pattern.compile("^rtsps?://([^/:]+)(?::(\\d+))*/([^/]+)/?([^*]*)$");

  private ConnectCheckerRtsp connectCheckerRtsp;
  //sockets objects
  private Socket connectionSocket;
  private BufferedReader reader;
  private BufferedWriter writer;
  private Thread thread;
  //for tcp
  private OutputStream outputStream;
  private volatile boolean streaming = false;
  //for secure transport
  private boolean tlsEnabled = false;
  private RtspSender rtspSender;
  private CommandsManager commandsManager;
  //reconnection
  private int reconnectionRetries = 0;
  private int retriesUsed = reconnectionRetries;
  private long reconnectionDelay = 0; //in milliseconds

  public RtspClient(ConnectCheckerRtsp connectCheckerRtsp) {
    this.connectCheckerRtsp = connectCheckerRtsp;
    commandsManager = new CommandsManager();
  }

  public void setReconnectionInfo(int retries, int delay) {
    reconnectionRetries = retries;
    retriesUsed = retries;
    reconnectionDelay = delay;
  }

  public void setProtocol(Protocol protocol) {
    commandsManager.setProtocol(protocol);
  }

  public void setAuthorization(String user, String password) {
    commandsManager.setAuth(user, password);
  }

  public boolean isStreaming() {
    return streaming;
  }

  public void setUrl(String url) {
    Matcher rtspMatcher = rtspUrlPattern.matcher(url);
    if (rtspMatcher.matches()) {
      tlsEnabled = rtspMatcher.group(0).startsWith("rtsps");
    } else {
      streaming = false;
      connectCheckerRtsp.onConnectionFailedRtsp(
          "Endpoint malformed, should be: rtsp://ip:port/appname/streamname");
      return;
    }
    String host = rtspMatcher.group(1);
    int port = Integer.parseInt((rtspMatcher.group(2) != null) ? rtspMatcher.group(2) : "554");
    String path = "/" + rtspMatcher.group(3) + "/" + rtspMatcher.group(4);
    commandsManager.setUrl(host, port, path);
  }

  public void setSampleRate(int sampleRate) {
    commandsManager.setSampleRate(sampleRate);
  }

  public String getHost() {
    return commandsManager.getHost();
  }

  public int getPort() {
    return commandsManager.getPort();
  }

  public String getPath() {
    return commandsManager.getPath();
  }

  public ConnectCheckerRtsp getConnectCheckerRtsp() {
    return connectCheckerRtsp;
  }

  public void setSPSandPPS(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {
    commandsManager.setVideoInfo(sps, pps, vps);
  }

  public void setIsStereo(boolean isStereo) {
    commandsManager.setIsStereo(isStereo);
  }

  public void connect() {
    connect(true);
  }

  public void connect(boolean resetRetries) {
    if (resetRetries) retriesUsed = reconnectionRetries;
    if (!streaming) {
      rtspSender = new RtspSender(this, commandsManager.getProtocol(), commandsManager.getSps(),
          commandsManager.getPps(), commandsManager.getVps(), commandsManager.getSampleRate());
      thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            if (!tlsEnabled) {
              connectionSocket = new Socket();
              SocketAddress socketAddress =
                  new InetSocketAddress(commandsManager.getHost(), commandsManager.getPort());
              connectionSocket.connect(socketAddress, 5000);
            } else {
              connectionSocket = CreateSSLSocket.createSSlSocket(commandsManager.getHost(),
                  commandsManager.getPort());
              if (connectionSocket == null) throw new IOException("Socket creation failed");
            }
            connectionSocket.setSoTimeout(5000);
            reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outputStream = connectionSocket.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(commandsManager.createOptions());
            writer.flush();
            commandsManager.getResponse(reader, connectCheckerRtsp, false, false);
            writer.write(commandsManager.createAnnounce());
            writer.flush();
            //check if you need credential for stream, if you need try connect with credential
            String response = commandsManager.getResponse(reader, connectCheckerRtsp, false, false);
            int status = commandsManager.getResponseStatus(response);
            if (status == 403) {
              connectCheckerRtsp.onConnectionFailedRtsp("Error configure stream, access denied");
              Log.e(TAG, "Response 403, access denied");
              return;
            } else if (status == 401) {
              Log.e("pedro", commandsManager.getUser() + "- -" + commandsManager.getPassword());
              if (commandsManager.getUser() == null || commandsManager.getPassword() == null) {
                connectCheckerRtsp.onAuthErrorRtsp();
                return;
              } else {
                writer.write(commandsManager.createAnnounceWithAuth(response));
                writer.flush();
                int statusAuth = commandsManager.getResponseStatus(
                    commandsManager.getResponse(reader, connectCheckerRtsp, false, false));
                if (statusAuth == 401) {
                  connectCheckerRtsp.onAuthErrorRtsp();
                  return;
                } else if (statusAuth == 200) {
                  connectCheckerRtsp.onAuthSuccessRtsp();
                } else {
                  connectCheckerRtsp.onConnectionFailedRtsp(
                      "Error configure stream, announce with auth failed");
                }
              }
            } else if (status != 200) {
              connectCheckerRtsp.onConnectionFailedRtsp("Error configure stream, announce failed");
            }
            writer.write(commandsManager.createSetup(commandsManager.getTrackAudio()));
            writer.flush();
            commandsManager.getResponse(reader, connectCheckerRtsp, true, true);
            writer.write(commandsManager.createSetup(commandsManager.getTrackVideo()));
            writer.flush();
            commandsManager.getResponse(reader, connectCheckerRtsp, false, true);
            writer.write(commandsManager.createRecord());
            writer.flush();
            commandsManager.getResponse(reader, connectCheckerRtsp, false, true);

            rtspSender.setDataStream(outputStream, commandsManager.getHost());
            int[] videoPorts = commandsManager.getVideoPorts();
            int[] audioPorts = commandsManager.getAudioPorts();
            rtspSender.setVideoPorts(videoPorts[0], videoPorts[1]);
            rtspSender.setAudioPorts(audioPorts[0], audioPorts[1]);
            rtspSender.start();
            streaming = true;
            retriesUsed = reconnectionRetries;
            connectCheckerRtsp.onConnectionSuccessRtsp();
          } catch (IOException | NullPointerException e) {
            if (retriesUsed > 0) {
              retriesUsed--;
              doRetry();
            } else {
              Log.e(TAG, "connection error", e);
              connectCheckerRtsp.onConnectionFailedRtsp(
                  "Error configure stream, " + e.getMessage());
              streaming = false;
            }
          }
        }
      });
      thread.start();
    }
  }

  private void doRetry() {
    try {
      Thread.sleep(reconnectionDelay);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    Log.i(TAG, "retry " + retriesUsed);
    connect(false);
  }

  public void disconnect() {
    if (thread != null) {
      thread.interrupt();
      try {
        thread.join(100);
      } catch (InterruptedException e) {
        thread.interrupt();
      }
    }
    retriesUsed = 0;
    if (streaming) {
      streaming = false;
      rtspSender.stop();
      thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            writer.write(commandsManager.createTeardown());
            connectionSocket.close();
          } catch (IOException e) {
            Log.e(TAG, "disconnect error", e);
          }
          connectCheckerRtsp.onDisconnectRtsp();
        }
      });
      thread.start();
      commandsManager.clear();
    }
  }

  public void sendVideo(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
    if (isStreaming()) {
      rtspSender.sendVideoFrame(h264Buffer, info);
    }
  }

  public void sendAudio(ByteBuffer aacBuffer, MediaCodec.BufferInfo info) {
    if (isStreaming()) {
      rtspSender.sendAudioFrame(aacBuffer, info);
    }
  }

  @Override
  public void onSendFailed() {
    if (retriesUsed > 0) {
      retriesUsed--;
      doRetry();
    } else {
      connectCheckerRtsp.onConnectionFailedRtsp("Send packet error");
    }
  }
}
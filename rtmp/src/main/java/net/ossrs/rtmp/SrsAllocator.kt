package net.ossrs.rtmp


internal class SrsAllocator @JvmOverloads constructor(private val individualAllocationSize: Int, initialAllocationCount: Int = 0) {

  @Volatile
  private var availableSentinel: Int = initialAllocationCount + 10
  private var availableAllocations: Array<Allocation?>

  /**
   * Constructs an instance with some [Allocation]s created up front.
   *
   *
   *
   * @param individualAllocationSize The length of each individual [Allocation].
   * @param initialAllocationCount The number of allocations to create up front.
   */
  /**
   * Constructs an instance without creating any [Allocation]s up front.
   *
   * @param individualAllocationSize The length of each individual [Allocation].
   */
  init {
    availableAllocations = arrayOfNulls(availableSentinel)
    for (i in 0 until availableSentinel) {
      availableAllocations[i] = Allocation(individualAllocationSize)
    }
  }

  @Synchronized
  fun allocate(size: Int): Allocation? {
    for (i in 0 until availableSentinel) {
      if (availableAllocations[i]!!.size() >= size) {
        val ret = availableAllocations[i]
        availableAllocations[i] = null
        return ret
      }
    }
    return Allocation(if (size > individualAllocationSize) size else individualAllocationSize)
  }

  @Synchronized
  fun release(allocation: Allocation) {
    allocation.clear()
    for (i in 0 until availableSentinel) {
      if (availableAllocations[i]!!.size() == 0) {
        availableAllocations[i] = allocation
        return
      }
    }
    if (availableSentinel + 1 > availableAllocations.size) {
      availableAllocations = availableAllocations.copyOf(availableAllocations.size * 2)
    }
    availableAllocations[availableSentinel++] = allocation
  }

  inner class Allocation(size: Int) {

    private val data: ByteArray = ByteArray(size)
    private var size: Int

    init {
      this.size = 0
    }

    fun array(): ByteArray {
      return data
    }

    fun size(): Int {
      return size
    }

    fun appendOffset(offset: Int) {
      size += offset
    }

    fun clear() {
      size = 0
    }

    fun put(b: Byte) {
      data[size++] = b
    }

    fun put(b: Byte, pos: Int) {
      var i = pos
      data[i++] = b
      size = if (i > size) i else size
    }

    fun put(s: Short) {
      put(s.toByte())
      put((s.toInt() ushr 8).toByte())
    }

    fun put(i: Int) {
      put(i.toByte())
      put((i ushr 8).toByte())
      put((i ushr 16).toByte())
      put((i ushr 24).toByte())
    }

    fun put(bs: ByteArray) {
      System.arraycopy(bs, 0, data, size, bs.size)
      size += bs.size
    }
  }
}
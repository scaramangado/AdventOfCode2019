package de.scaramangado.intcode

open class IntExchange(private val debug: Boolean = false) {

  private val buffer = mutableListOf<Int>()

  open fun addInt(i: Int) {
    if (debug) println("Add $i")
    buffer.add(i)
  }

  open fun readInt(): Int {

    while (buffer.isEmpty()) Thread.sleep(10)
    return buffer.removeAt(0).also { if (debug) println("Take $it") }
  }
}

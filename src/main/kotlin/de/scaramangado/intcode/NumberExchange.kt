package de.scaramangado.intcode

abstract class NumberExchange<T: Number>(private val debug: Boolean = false) {

  private val buffer = mutableListOf<T>()

  open fun addNumber(i: T) {
    if (debug) println("Add $i")
    buffer.add(i)
  }

  open fun readNumber(): T {

    while (buffer.isEmpty()) Thread.sleep(3)
    return buffer.removeAt(0).also { if (debug) println("Take $it") }
  }
}

package de.scaramangado.intcode

abstract class NumberExchange<T : Number>(private val debugMode: DebugMode = DebugMode.NONE) {

  private val buffer = mutableListOf<T>()
  private var done = false

  open fun addNumber(i: T) {
    if (debugMode == DebugMode.LOG) println("Add $i")
    buffer.add(i)
  }

  open fun readNumber(): T {

    if (debugMode == DebugMode.SLEEP) Thread.sleep(4)
    while (buffer.isEmpty()) Thread.sleep(3)
    return buffer.removeAt(0).also { if (debugMode == DebugMode.LOG) println("Take $it") }
  }

  fun running() = !done

  fun done() {
    done = true
  }

  enum class DebugMode {
    NONE, LOG, SLEEP
  }
}

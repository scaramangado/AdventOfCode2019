package de.scaramangado.intcode

class SingleValueExchange(debugMode: DebugMode = DebugMode.NONE) : NumberExchange<Int>(debugMode) {

  private var number: Int? = null

  override fun readNumber(): Int {

    if (debugMode == DebugMode.LOG) println("S Try take")
    if (debugMode == DebugMode.SLEEP) Thread.sleep(4)
    while (number == null) Thread.sleep(3)
    return number!!
        .also { if (debugMode == DebugMode.LOG) println("S Take $it") }
        .also { number = null }
  }

  override fun addNumber(i: Int) {
    if (debugMode == DebugMode.LOG) println("S Add $i")
    number = i
  }
}

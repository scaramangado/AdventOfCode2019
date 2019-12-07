package de.scaramangado.intcode

class ConsoleIntExchange: IntExchange() {

  override fun addInt(i: Int) = println("IntCode Output: $i")

  override fun readInt(): Int {
    println("Enter a Number:")
    return readLine()?.toInt() ?: throw IllegalArgumentException()
  }
}

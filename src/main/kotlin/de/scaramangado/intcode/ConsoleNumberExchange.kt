package de.scaramangado.intcode

class ConsoleNumberExchange: NumberExchange<Long>() {

  override fun addNumber(i: Long) = println("IntCode Output: $i")

  override fun readNumber(): Long {
    println("Enter a Number:")
    return readLine()?.toLong() ?: throw IllegalArgumentException()
  }
}

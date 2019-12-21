package de.scaramangado.day21

import de.scaramangado.day1.Day1
import de.scaramangado.intcode.CPU
import de.scaramangado.intcode.IntExchange

fun main() {
  println("Answer 1:\n${SpringDroid(originalInput).countHolesWalking()}")
  println("Answer 2:\n${SpringDroid(originalInput).countHolesRunning()}")
}

private class SpringDroid(private val intCode: List<Long>) {

  fun countHolesWalking(print: Boolean = false): Int {

    val writeToCpu = IntExchange()
    val readFromCpu = IntExchange()

    listOf("NOT A T",
           "NOT B J",
           "OR J T",
           "NOT C J",
           "OR T J",
           "AND D J",
           "WALK")
        .joinToString("") { "$it\n" }
        .chars()
        .forEach { writeToCpu.addNumber(it) }

    val cpu = CPU(intCode, writeToCpu, readFromCpu)
    cpu.compute()

    var lastNumber = 0

    while (readFromCpu.ready()) {
      lastNumber = readFromCpu.readNumber()
      if (print) {
        print(lastNumber.toChar())
      }
    }

    return lastNumber
  }

  fun countHolesRunning(print: Boolean = false): Int {

    val writeToCpu = IntExchange()
    val readFromCpu = IntExchange()

    listOf("NOT A T",
           "NOT B J",
           "OR J T",
           "NOT C J",
           "OR T J",
           "AND D J",
           "NOT E T",
           "NOT T T",
           "OR H T",
           "AND T J",
           "RUN")
        .joinToString("") { "$it\n" }
        .chars()
        .forEach { writeToCpu.addNumber(it) }

    val cpu = CPU(intCode, writeToCpu, readFromCpu)
    cpu.compute()

    var lastNumber = 0

    while (readFromCpu.ready()) {
      lastNumber = readFromCpu.readNumber()
      if (print) {
        print(lastNumber.toChar())
      }
    }

    return lastNumber
  }
}

private val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day21/input_puzzle_1")
      ?.readText()
      ?.split(",")
      ?.map { it.toLong() }
      ?: emptyList()
}

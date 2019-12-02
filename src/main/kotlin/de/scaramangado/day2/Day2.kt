package de.scaramangado.day2

import de.scaramangado.day1.Day1
import java.lang.IllegalStateException

fun main() {

  val input = readInput().toMutableList()
  input[1] = 12
  input[2] = 2

  println(compute(input))
}

private fun compute(intCode: List<Int>): List<Int> {

  val code = intCode.toMutableList()
  var pointer = 0

  while (code[pointer] != 99) {
    val operation: Int.(Int) -> Int = when {
      code[pointer] == 1 -> Int::plus
      code[pointer] == 2 -> Int::times
      else -> throw IllegalStateException()
    }

    code[code[pointer + 3]] = code[code[pointer + 1]].operation(code[code[pointer + 2]])
    pointer += 4
  }

  return code.toList()
}

private fun readInput(): List<Int> =
    Day1::class.java.classLoader.getResource("day2/input_puzzle_1")?.readText()
        ?.split(",")
        ?.map { it.toInt() }
        ?: emptyList()

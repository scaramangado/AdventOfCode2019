package de.scaramangado.day2

import de.scaramangado.day1.Day1
import kotlin.streams.toList

fun main() {

  printAnswer1()
  println()
  printAnswer2()
}

private fun printAnswer1() {
  val input = originalInput.toMutableList()
  input[1] = 12
  input[2] = 2

  println("Answer 1:")
  println(compute(input))
}

private fun printAnswer2() {

  val matches = allPairs()
      .parallelStream()
      .map { it to initialState(it) }
      .map { it.first to compute(it.second) }
      .map { it.first to it.second[0] }
      .filter { it.second == 19690720 }
      .toList()

  require(matches.size == 1)

  println("Answer 2:")
  println(matches[0].first.let { 100 * it.first + it.second })
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

private fun allPairs(): List<Pair<Int, Int>> {

  val allNumbers = 0..99
  val pairs = mutableListOf<Pair<Int, Int>>()

  allNumbers.forEach {
    for (number in allNumbers) {
      pairs.add(it to number)
    }
  }

  return pairs
}

private fun initialState(values: Pair<Int, Int>): List<Int> {
  val input = originalInput.toMutableList()
  input[1] = values.first
  input[2] = values.second

  return input
}

val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day2/input_puzzle_1")?.readText()
      ?.split(",")
      ?.map { it.toInt() }
      ?: emptyList()
}

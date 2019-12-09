package de.scaramangado.day9

import de.scaramangado.day1.Day1
import de.scaramangado.intcode.CPU

fun main() {
  // Input = Puzzle number
  CPU(originalInput).compute()
}

val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day9/input_puzzle_1")?.readText()
      ?.split(",")
      ?.map { it.toLong() }
      ?: emptyList()
}

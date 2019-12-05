package de.scaramangado.day5

import de.scaramangado.day1.Day1
import de.scaramangado.intcode.CPU

fun main() {

  // Question 1: input 0; Question 2: input 5
  CPU(originalInput).compute()
}

val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day5/input_puzzle_1")?.readText()
      ?.split(",")
      ?.map { it.toInt() }
      ?: emptyList()
}

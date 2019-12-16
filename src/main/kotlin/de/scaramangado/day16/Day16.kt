package de.scaramangado.day16

import de.scaramangado.day1.Day1
import kotlin.math.abs

fun main() {

  originalInput.calculatePhase(100).joinToString("").substring(0..7)
      .also { println("Answer 1:\n$it") }

  (1..10000).flatMap { originalInput }.calculatePhaseUsingLargeOffset(100, offset(originalInput))
      .joinToString("").substring(0..7).also { println("Answer 2:\n$it") }
}

private fun List<Int>.calculatePhaseUsingLargeOffset(number: Int, offset: Int): List<Int> {

  require(number >= 1 && offset > this.size / 2)

  var lastIteration = this.subList(offset, this.size)

  for (i in 0 until number) {
    val currentIteration = lastIteration.toMutableList()

    for (currentIndex in (0 until currentIteration.size - 1).reversed()) {
      currentIteration[currentIndex] =
          abs((currentIteration[currentIndex + 1] + lastIteration[currentIndex])%10)
    }

    lastIteration = currentIteration.toList()
  }

  return lastIteration
}

private fun offset(ints: List<Int>) =
    (0..6).map { ints[it] }.joinToString("").toInt()

private fun List<Int>.calculatePhase(number: Int): List<Int> {

  fun pattern(run: Int, index: Int): Int =
      basePattern[((index + 1) / run) % basePattern.size]

  require(number >= 1)

  var lastIteration = this.toList()

  for (i in 0 until number) {
    val currentIteration = lastIteration.toMutableList()

    for (currentIndex in 0 until currentIteration.size) {
      currentIteration[currentIndex] = lastIteration
          .mapIndexed { lastIndex, n -> n * pattern(currentIndex + 1, lastIndex) }
          .sum()
          .let { abs(it % 10) }
    }

    lastIteration = currentIteration.toList()
  }

  return lastIteration
}

private val basePattern = listOf(0, 1, 0, -1)

private val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day16/input_puzzle_1")?.readText()
      ?.toCharArray()?.map { it.toString().toInt() } ?: emptyList()
}

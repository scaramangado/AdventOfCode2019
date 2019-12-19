package de.scaramangado.day19

import de.scaramangado.day1.Day1
import de.scaramangado.intcode.CPU
import de.scaramangado.intcode.IntExchange
import java.util.stream.IntStream

fun main() {

  (0..49)
      .flatMap { x ->
        (0..49).map { y -> x to y }
      }
      .map { deployDrone(it.first, it.second) }
      .sum()
      .run { println("Answer 1:\n$this") }

  val shipSize = 100

  IntStream.iterate(4) { it + 1 }
      .parallel()
      .mapToObj { y ->
        IntStream.iterate(0) { it + 1 }
            .filter { x -> deployDrone(x, y) == 1 }
            .findFirst().orElseThrow { RuntimeException() } to y
      }
      .filter { deployDrone(it.first + shipSize - 1, it.second) == 1 }
      .map {
        IntStream.iterate(it.first) { x -> x + 1 }
            .limit(it.first.toLong())
            .filter { x -> deployDrone(x + shipSize - 1, it.second) == 1 }
            .filter { x -> deployDrone(x, it.second + shipSize - 1) == 1 }
            .findFirst().let { x -> if (x.isPresent) x.asInt else null } to it.second
      }
      .filter { it.first != null }
      .findFirst().orElseThrow { RuntimeException() }
      .let { 10000 * it.first!! + it.second }
      .run { println("Test: $this") }
}

fun deployDrone(x: Int, y: Int): Int {

  require(x >= 0 && y >= 0)

  val writeToCpu = IntExchange().also {
    it.addNumber(x)
    it.addNumber(y)
  }

  val readFromCpu = IntExchange()

  CPU(originalInput, writeToCpu, readFromCpu).compute()

  return readFromCpu.readNumber()
}

private val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day19/input_puzzle_1")
      ?.readText()
      ?.split(",")
      ?.map { it.toLong() }
      ?: emptyList()
}

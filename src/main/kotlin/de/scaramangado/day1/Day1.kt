package de.scaramangado.day1

import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.floor
import kotlin.math.roundToInt

class Day1

fun main() {

  println("Answer 1:")
  println(readInput()
              .asSequence()
              .map { calculateFuelNeeded(it) }
              .sum())
}

private fun calculateFuelNeeded(mass: Int): Int =
    floor(mass / 3.0).roundToInt() - 2

private fun readInput(): List<Int> =
    Day1::class.java.classLoader.getResource("day1/input_puzzle_1")?.readText()
        ?.split("\n")
        ?.map { it.toInt() }
        ?: emptyList()

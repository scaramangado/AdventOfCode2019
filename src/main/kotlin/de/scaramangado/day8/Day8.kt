package de.scaramangado.day8

import de.scaramangado.day1.Day1

fun main() {
  getLayers(originalInput, 6, 25)
      .also { input ->
        input.minBy { it.count { n -> n == 0 } }
            ?.let { it.count { n -> n == 1 } * it.count { n -> n == 2 } }
            ?.run { println("Answer 1:\n$this") }
      }
      .let { decodeImage(it) }
      .run {
        println("\nImage:\n")
        displayImage(this, 25)
      }
}

fun getLayers(image: String, height: Int, width: Int): List<List<Int>> {
  return image.toCharArray()
      .map { it.toString() }
      .map { it.toInt() }
      .chunked(height * width)
}

fun decodeImage(layers: List<List<Int>>): List<Int> {

  return layers[0].indices
      .map {
        layers
            .asSequence()
            .map { layer -> layer[it] }
            .filter { n -> n != 2 }
            .first()
      }
}

fun displayImage(image: List<Int>, width: Int) {
  image.map {
    when (it) {
      0 -> " "
      1 -> "\u2588"
      else -> throw IllegalStateException()
    }
  }.chunked(width).map { it.joinToString("") }.forEach { println(it) }
}

val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day8/input_puzzle_1")?.readText() ?: ""
}

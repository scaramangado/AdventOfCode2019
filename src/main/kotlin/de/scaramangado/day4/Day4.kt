package de.scaramangado.day4

fun main() {

  (387638..919123)
      .map { figures(it) }
      .filter { hasDoubleDigit(it) }
      .filter { figuresAreMonotone(it) }
      .also { println("Answer 1:\n${it.count()}") }
      .filter { hasNoMoreThanDoubleDigits(it) }
      .run { println("Answer 2:\n${count()}") }
}

fun hasDoubleDigit(figures: List<Int>): Boolean =
    figures.findLast { figures.count { f -> f == it } > 1 } != null

fun hasNoMoreThanDoubleDigits(figures: List<Int>): Boolean =
    figures.findLast { figures.count { f -> f == it } == 2 } != null

fun figuresAreMonotone(figures: List<Int>): Boolean =
    figures == figures.sorted()

fun figures(number: Int): List<Int> =
    number.toString()
        .toCharArray()
        .map { it.toInt() }

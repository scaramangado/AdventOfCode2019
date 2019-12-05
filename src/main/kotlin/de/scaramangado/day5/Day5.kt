package de.scaramangado.day5

import de.scaramangado.intcode.CPU

fun main() {
  CPU(listOf(3, 0, 4, 0, 99)).compute()
}

package de.scaramangado.intcode

class CPU(private val intCode: List<Int>) {

  fun compute(): List<Int> {

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
}

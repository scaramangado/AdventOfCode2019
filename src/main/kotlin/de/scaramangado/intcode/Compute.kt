package de.scaramangado.intcode

class CPU(private val intCode: List<Int>) {

  fun compute(): List<Int> {

    val code = intCode.toMutableList()
    var pointer = 0

    while (code[pointer] != 99) {
      pointer = when (getOpType(code[pointer])) {
        OpType.BI_FUNCTION -> runBiFunction(pointer, code)
        OpType.FUNCTION -> runFunction(pointer, code)
        OpType.RUNNABLE -> runRunnable(pointer, code)
        OpType.TERMINAL -> return code.toList()
      }
    }

    return code.toList()
  }

  private fun runBiFunction(pointer: Int, intCode: MutableList<Int>): Int {

    val operation: Int.(Int) -> Int = when {
      intCode[pointer] == 1 -> Int::plus
      intCode[pointer] == 2 -> Int::times
      else -> throw IllegalStateException()
    }

    intCode[intCode[pointer + 3]] = intCode[intCode[pointer + 1]].operation(intCode[intCode[pointer + 2]])

    return pointer + 4
  }

  private fun runFunction(pointer: Int, intCode: MutableList<Int>): Int {
    require(intCode[pointer] == 3)
    println("Enter a Number:")
    intCode[intCode[pointer + 1]] = readLine()?.toInt() ?: throw IllegalArgumentException("Number must be entered.")
    return pointer + 2
  }

  private fun runRunnable(pointer: Int, intCode: MutableList<Int>): Int {
    require(intCode[pointer] == 4)
    println("IntCode Output: ${intCode[intCode[pointer + 1]]}")
    return pointer + 2
  }

  enum class OpType {
    BI_FUNCTION, FUNCTION, RUNNABLE, TERMINAL
  }

  private fun getOpType(operation: Int): OpType =
      when (operation) {
        in 1..2 -> OpType.BI_FUNCTION
        3 -> OpType.FUNCTION
        4 -> OpType.RUNNABLE
        99 -> OpType.TERMINAL
        else -> throw IllegalArgumentException("OpCode $operation does not exist")
      }
}

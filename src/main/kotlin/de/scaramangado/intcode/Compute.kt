package de.scaramangado.intcode

class CPU(private val intCode: List<Int>) {

  fun compute(): List<Int> {

    val code = intCode.toMutableList()
    var pointer = 0

    while (true) {
      pointer = Operation(pointer, code).run()
      if (pointer < 0) return code.toList()
    }
  }
}

private class Operation(private val pointer: Int, private val intCode: MutableList<Int>) {

  private val operationAsList by lazy {
    intCode[pointer].toString().toCharArray().reversed().map { it.toString().toInt() }.let { ZeroIntList(it) }
        .let { mutableListOf((it[0] + 10 * it[1]), it.subList(2, it.size)) }
  }

  private val opCode by lazy { operationAsList[0] }

  private fun parameterMode(parameterNum: Int) =
      when (operationAsList[parameterNum + 1]) {
        0 -> ParameterMode.POSITION
        1 -> ParameterMode.IMMEDIATE
        else -> throw IllegalArgumentException()
      }

  private fun getOpType(): OpType =
      when (opCode) {
        in 1..2 -> OpType.BI_FUNCTION
        3 -> OpType.FUNCTION
        4 -> OpType.RUNNABLE
        99 -> OpType.TERMINAL
        else -> throw IllegalArgumentException("OpCode $opCode does not exist")
      }

  private inner class ZeroIntList(private val ints: List<Int>) {
    operator fun get(i: Int): Int = if (i >= ints.size) 0 else ints[i]
    val size = ints.size
    fun subList(start: Int, end: Int) = ZeroIntList(
        when {
          start >= ints.size -> emptyList()
          end > ints.size -> ints.subList(start, ints.size)
          else -> ints.subList(start, end)
        }
    )
  }

  fun run(): Int {
    return when (getOpType()) {
      OpType.BI_FUNCTION -> runBiFunction()
      OpType.FUNCTION -> runFunction()
      OpType.RUNNABLE -> runRunnable()
      OpType.TERMINAL -> -1
    }
  }

  private fun runBiFunction(): Int {

    val operation: Int.(Int) -> Int = when {
      intCode[pointer] == 1 -> Int::plus
      intCode[pointer] == 2 -> Int::times
      else -> throw IllegalStateException()
    }

    intCode[intCode[pointer + 3]] = intCode[intCode[pointer + 1]].operation(intCode[intCode[pointer + 2]])

    return pointer + 4
  }

  private fun runFunction(): Int {
    require(intCode[pointer] == 3)
    println("Enter a Number:")
    intCode[intCode[pointer + 1]] = readLine()?.toInt() ?: throw IllegalArgumentException("Number must be entered.")
    return pointer + 2
  }

  private fun runRunnable(): Int {
    require(intCode[pointer] == 4)
    println("IntCode Output: ${intCode[intCode[pointer + 1]]}")
    return pointer + 2
  }

  enum class ParameterMode {
    POSITION, IMMEDIATE
  }

  enum class OpType {
    BI_FUNCTION, FUNCTION, RUNNABLE, TERMINAL
  }
}

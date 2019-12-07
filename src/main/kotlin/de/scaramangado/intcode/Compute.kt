package de.scaramangado.intcode

class CPU(private val intCode: List<Int>, private val input: IntExchange = ConsoleIntExchange(),
          private val output: IntExchange = ConsoleIntExchange()) {

  fun compute(): List<Int> {

    val code = intCode.toMutableList()
    var pointer = 0

    while (true) {
      pointer = Operation(pointer, code, input, output).run()
      if (pointer < 0) return code.toList()
    }
  }
}

private class Operation(private val pointer: Int, private val intCode: MutableList<Int>, private val input: IntExchange,
                        private val output: IntExchange) {

  private val operationAsList by lazy {
    intCode[pointer].toString().toCharArray().reversed().map { it.toString().toInt() }.let { ZeroIntList(it) }
        .let { ZeroIntList(listOf((it[0] + 10 * it[1]), *it.subList(2, it.size).toTypedArray())) }
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
        in listOf(1, 2, 7, 8) -> OpType.BI_FUNCTION
        3 -> OpType.FUNCTION
        4 -> OpType.RUNNABLE
        in listOf(5, 6) -> OpType.CONDITIONAL
        99 -> OpType.TERMINAL
        else -> throw IllegalArgumentException("OpCode $opCode does not exist")
      }

  private inner class ZeroIntList(private val ints: List<Int>) {
    operator fun get(i: Int): Int = if (i >= ints.size) 0 else ints[i]
    val size = ints.size
    fun subList(start: Int, end: Int) =
        when {
          start >= ints.size -> emptyList()
          end > ints.size -> ints.subList(start, ints.size)
          else -> ints.subList(start, end)
        }
  }

  fun run(): Int {
    return when (getOpType()) {
      OpType.BI_FUNCTION -> runBiFunction()
      OpType.FUNCTION -> runFunction()
      OpType.RUNNABLE -> runRunnable()
      OpType.CONDITIONAL -> runConditional()
      OpType.TERMINAL -> -1
    }
  }

  private fun getArgument(n: Int) =
      when (parameterMode(n)) {
        ParameterMode.POSITION -> intCode[intCode[pointer + n + 1]]
        ParameterMode.IMMEDIATE -> intCode[pointer + n + 1]
      }

  private fun runBiFunction(): Int {

    intCode[intCode[pointer + 3]] =
        when (opCode) {
          1 -> getArgument(0) + getArgument(1)
          2 -> getArgument(0) * getArgument(1)
          7 -> if (getArgument(0) < getArgument(1)) 1 else 0
          8 -> if (getArgument(0) == getArgument(1)) 1 else 0
          else -> throw IllegalStateException()
        }

    return pointer + 4
  }

  private fun runFunction(): Int {
    require(opCode == 3)
    intCode[intCode[pointer + 1]] = input.readInt()
    return pointer + 2
  }

  private fun runRunnable(): Int {
    require(opCode == 4)
    output.addInt(getArgument(0))
    return pointer + 2
  }

  private fun runConditional(): Int {

    val check = when (opCode) {
      5 -> getArgument(0) != 0
      6 -> getArgument(0) == 0
      else -> throw IllegalArgumentException()
    }

    return if (check) getArgument(1) else pointer + 3
  }

  enum class ParameterMode {
    POSITION, IMMEDIATE
  }

  enum class OpType {
    BI_FUNCTION, FUNCTION, RUNNABLE, CONDITIONAL, TERMINAL
  }
}

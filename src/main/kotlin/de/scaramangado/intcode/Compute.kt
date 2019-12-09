package de.scaramangado.intcode

class CPU(private val intCode: List<Long>, private val input: NumberExchange<out Number> = ConsoleNumberExchange(),
          private val output: NumberExchange<out Number> = ConsoleNumberExchange()) {

  constructor(intCode: List<Int>) : this(intCode.map { it.toLong() })
  constructor(intCode: List<Int>, input: IntExchange, output: IntExchange) :
      this(intCode.map { it.toLong() }, input, output)

  fun compute(): List<Long> {

    val code = ZeroList(intCode)
    var state = CpuState()

    while (true) {
      state = Operation(state, code, input, output).run()
      if (state.pointer < 0) return code.toList()
    }
  }
}

private class Operation(private val state: CpuState, private val intCode: ZeroList<Long>,
                        private val input: NumberExchange<out Number>, private val output: NumberExchange<out Number>) {

  private val operationAsList by lazy {
    intCode[state.pointer].toString().toCharArray().reversed().map { it.toString().toInt() }.let { ZeroIntList(it) }
        .let { ZeroIntList(listOf((it[0] + 10 * it[1]), *it.subList(2, it.size).toTypedArray())) }
  }

  private val opCode by lazy { operationAsList[0] }

  private fun parameterMode(parameterNum: Int) =
      when (operationAsList[parameterNum + 1]) {
        0 -> ParameterMode.POSITION
        1 -> ParameterMode.IMMEDIATE
        2 -> ParameterMode.RELATIVE
        else -> throw IllegalArgumentException()
      }

  private fun getOpType(): OpType =
      when (opCode) {
        in listOf(1, 2, 7, 8) -> OpType.BI_FUNCTION
        3 -> OpType.FUNCTION
        in listOf(4, 9) -> OpType.RUNNABLE
        in listOf(5, 6) -> OpType.CONDITIONAL
        99 -> OpType.TERMINAL
        else -> throw IllegalArgumentException("OpCode $opCode does not exist")
      }

  fun run(): CpuState {
    return when (getOpType()) {
      OpType.BI_FUNCTION -> runBiFunction()
      OpType.FUNCTION -> runFunction()
      OpType.RUNNABLE -> runRunnable()
      OpType.CONDITIONAL -> runConditional()
      OpType.TERMINAL -> state.withPointer(-1)
    }
  }

  private fun getArgument(n: Int) =
      when (parameterMode(n)) {
        ParameterMode.POSITION -> intCode[intCode[state.pointer + n + 1].toInt()]
        ParameterMode.IMMEDIATE -> intCode[state.pointer + n + 1]
        ParameterMode.RELATIVE -> intCode[state.base + intCode[state.pointer + n + 1].toInt()]
      }

  private fun getWriteAddress(n: Int): Int =
      when (parameterMode(n)) {
        ParameterMode.POSITION -> intCode[state.pointer + n + 1].toInt()
        ParameterMode.RELATIVE -> state.base + intCode[state.pointer + n + 1].toInt()
        ParameterMode.IMMEDIATE -> throw UnsupportedOperationException()
      }

  private fun runBiFunction(): CpuState {

    intCode[getWriteAddress(2)] =
        when (opCode) {
          1 -> getArgument(0) + getArgument(1)
          2 -> getArgument(0) * getArgument(1)
          7 -> if (getArgument(0) < getArgument(1)) 1L else 0L
          8 -> if (getArgument(0) == getArgument(1)) 1L else 0L
          else -> throw IllegalStateException()
        }

    return state addToPointer 4
  }

  private fun runFunction(): CpuState {
    require(opCode == 3)
    intCode[getWriteAddress(0)] = input.readNumber().toLong()
    return state addToPointer 2
  }

  private fun runRunnable(): CpuState {

    return when (opCode) {
      4 -> {
        with(output) {
          when (this) {
            is IntExchange -> addNumber(getArgument(0).toInt())
            is ConsoleNumberExchange -> addNumber(getArgument(0))
            else -> IllegalStateException("Unknown NumberExchange implementation")
          }
        }
        state
      }

      9 -> state addToBase getArgument(0).toInt()
      else -> throw IllegalStateException()
    } addToPointer 2
  }

  private fun runConditional(): CpuState {

    val check = when (opCode) {
      5 -> getArgument(0) != 0L
      6 -> getArgument(0) == 0L
      else -> throw IllegalArgumentException()
    }

    return if (check) state.withPointer(getArgument(1).toInt()) else state addToPointer 3
  }

  enum class ParameterMode {
    POSITION, IMMEDIATE, RELATIVE
  }

  enum class OpType {
    BI_FUNCTION, FUNCTION, RUNNABLE, CONDITIONAL, TERMINAL
  }
}

class CpuState(val pointer: Int = 0, val base: Int = 0) {

  fun withPointer(newPointer: Int) = CpuState(newPointer, base)
  private fun withBase(newBase: Int) = CpuState(pointer, newBase)

  infix fun addToPointer(add: Int) = withPointer(pointer + add)
  infix fun addToBase(add: Int) = withBase(base + add)

  override fun toString(): String = "$pointer; $base"
}

private class ZeroIntList(ints: List<Int>): ZeroList<Int>(ints)

private open class ZeroList<T: Number>(private val numbers: ArrayList<T>) {

  constructor(numbers: List<T>) : this(ArrayList(numbers))

  @Suppress("UNCHECKED_CAST")
  operator fun get(i: Int): T = if (i >= numbers.size) 0 as T else numbers[i]

  @Suppress("UNCHECKED_CAST")
  operator fun set(i: Int, n: T) {
    if (i < numbers.size) {
      numbers[i] = n
    } else {
      numbers.addAll((numbers.size until i).map { 0 as T })
      numbers.add(n)
    }
  }

  val size = numbers.size

  fun subList(start: Int, end: Int) =
      when {
        start >= numbers.size -> ArrayList()
        end > numbers.size -> numbers.subList(start, numbers.size)
        else -> numbers.subList(start, end)
      }

  fun toList(): List<T> = numbers.toList()
}

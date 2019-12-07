package de.scaramangado.day7

import de.scaramangado.day1.Day1
import de.scaramangado.intcode.CPU
import de.scaramangado.intcode.IntExchange
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.streams.toList

fun main() {

  val writeToCpu = IntExchange()
  val readFromCpu = IntExchange()

  val permutationsProblem1 = listOf(0, 1, 2, 3, 4).permutations()

  permutationsProblem1.map { sequence ->

    var lastOutput = 0
    sequence.forEach {
      writeToCpu.addInt(it)
      writeToCpu.addInt(lastOutput)
      CPU(originalInput, writeToCpu, readFromCpu).compute()
      lastOutput = readFromCpu.readInt()
    }
    lastOutput
  }.max().run { println("Answer 1:\n$this") }

  val permutationsProblem2 = listOf(5, 6, 7, 8, 9).permutations()

  permutationsProblem2.parallelStream().map { sequence ->

    val finished = AtomicBoolean(false)

    val readA = IntExchange()
    val readB = IntExchange()
    val readC = IntExchange()
    val readD = IntExchange()
    val readE = IntExchange()

    readE.addInt(sequence[0])
    readE.addInt(0)
    readA.addInt(sequence[1])
    readB.addInt(sequence[2])
    readC.addInt(sequence[3])
    readD.addInt(sequence[4])

    thread {
      CPU(originalInput, readE, readA).compute()
    }
    thread {
      CPU(originalInput, readA, readB).compute()
    }
    thread {
      CPU(originalInput, readB, readC).compute()
    }
    thread {
      CPU(originalInput, readC, readD).compute()
    }
    thread {
      CPU(originalInput, readD, readE).compute()
      finished.set(true)
    }

    while (!finished.get()) Thread.sleep(5)
    readE.readInt()
  }.toList().max().run { println("Answer 2:\n$this") }
}

val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day7/input_puzzle_1")?.readText()
      ?.split(",")
      ?.map { it.toInt() }
      ?: emptyList()
}

fun List<Int>.permutations(): List<List<Int>> {

  val perms = mutableListOf<List<Int>>()

  fun permutations(n: Int, list: MutableList<Int>) {

    fun swap(list: MutableList<Int>, index1: Int, index2: Int) {
      val temp = list[index1]
      list[index1] = list[index2]
      list[index2] = temp
    }

    if (n == 1) {
      perms.add(list.toList())
      return
    }

    for (i in 0 until n - 1) {
      permutations(n - 1, list)
      if (n % 2 == 0) {
        swap(list, i, n - 1)
      } else {
        swap(list, 0, n - 1)
      }
    }

    permutations(n - 1, list)
  }

  permutations(this.size, this.toMutableList())
  return perms.toList()
}

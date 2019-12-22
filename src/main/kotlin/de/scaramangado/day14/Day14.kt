package de.scaramangado.day14

import de.scaramangado.day1.Day1
import de.scaramangado.day13.uniqueResult
import kotlin.math.ceil

fun main() {

  originalInput
      .let { EquationSystemSolver(it).solveForOre() }
      .also { println("Answer1:\n$it") }

  println("Answer 2:\n${searchMaxFuel(1, 1000000000000)}")
}

fun searchMaxFuel(minFuel: Long, maxFuel: Long): Long {

  val oreStored = 1000000000000

  fun oreNeeded(fuelAmount: Long) =
      EquationSystemSolver(originalInput, listOf(Quantity(fuelAmount, "FUEL"))).solveForOre()

  fun search(minFuel: Long, maxFuel: Long): Long {
    if (minFuel == maxFuel - 1) {
      return minFuel
    }

    val middle = (minFuel + maxFuel) / 2
    val middleOre = oreNeeded(middle)

    return when {
      middleOre == oreStored -> middle
      middleOre < oreStored -> search(middle, maxFuel)
      middleOre > oreStored -> search(minFuel, middle)
      else -> throw IllegalStateException()
    }
  }

  require(oreNeeded(minFuel) < oreStored && oreNeeded(maxFuel) > oreStored)

  return search(minFuel, maxFuel)
}

private class EquationSystemSolver(private val reactions: List<Reaction>,
                                   neededChemicals: List<Quantity> = listOf(Quantity(1, "FUEL"))) {

  private val storage = mutableListOf<Quantity>()
  private val needed = neededChemicals.toMutableList()

  fun solveForOre(): Long {

    while (needed.size > 1 || needed[0].chemical != "ORE") {
      needed.filter { it.chemical != "ORE" }.getOrNull(0)?.run { produce(this) }
    }

    return needed[0].amount
  }

  private fun produce(quantity: Quantity) {

    val reaction = reactions
        .filter { it.outputChemical.chemical == quantity.chemical }
        .uniqueResult()
        .also { requireNotNull(it) }!!

    val reactionsNeeded = ceil(quantity.amount.toDouble() / reaction.outputChemical.amount).toLong()

    (reaction * reactionsNeeded).inputChemicals
        .forEach {

          val amountStored = storage
              .filter { s -> s.chemical == it.chemical }
              .uniqueResult()
              ?.amount
              ?: 0

          val removedFromStorage =
              if (it.amount > amountStored) amountStored
              else it.amount

          (needed
              .filter { n -> n.chemical == it.chemical }
              .uniqueResult()
              ?.also { n -> needed.remove(n) }
              ?: Quantity(0, it.chemical))
              .let { q -> q + (it.amount - removedFromStorage) }
              .run { needed.add(this) }

          if (removedFromStorage > 0) {
            storage.filter { s -> s.chemical == it.chemical }.uniqueResult()!!
                .also { s -> require(s.amount >= removedFromStorage) }
                .also { s -> storage.remove(s) }
                .let { q -> q - amountStored }
                .run { storage.add(this) }
          }
        }

    needed.remove(quantity)

    reactionsNeeded
        .let { (it * reaction.outputChemical.amount) - quantity.amount }
        .let { Quantity(it, quantity.chemical) }
        .run {

          storage.filter { s -> s.chemical == this.chemical }
              .forEach { storage.remove(it) }

          storage.add(this)
        }
  }
}

private class Reaction(val inputChemicals: List<Quantity>, val outputChemical: Quantity) {
  operator fun times(other: Long) = Reaction(inputChemicals.map { it * other }, outputChemical * other)
  override fun toString() = "${inputChemicals.joinToString(", ") { it.toString() }} => $outputChemical"
}

private class Quantity(val amount: Long, val chemical: String) {
  operator fun times(other: Long) = Quantity(other * amount, chemical)
  operator fun plus(other: Long) = Quantity(amount + other, chemical)
  operator fun minus(other: Long) = Quantity(amount - other, chemical)
  override fun toString() = "$amount $chemical"
}

private fun String.toQuantity(): Quantity = this.split(" ").let { Quantity(it[0].toLong(), it[1]) }

private val originalInput by lazy {
  Day1::class.java.classLoader.getResource("day14/input_puzzle_1")
      ?.readText()
      ?.lines()
      ?.asSequence()
      ?.map { it.split(" => ") }
      ?.map { it[0] to it[1] }
      ?.map { it.first.split(", ").map { s -> s.toQuantity() } to it.second.toQuantity() }
      ?.map { Reaction(it.first, it.second) }
      ?.plus(Reaction(listOf(Quantity(1, "ORE")), Quantity(1, "ORE")))
      ?.toList()
      ?: emptyList()
}

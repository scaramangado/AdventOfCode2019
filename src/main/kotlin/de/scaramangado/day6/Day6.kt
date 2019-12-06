package de.scaramangado.day6

import de.scaramangado.day1.Day1

fun main() {
  readData()

  bodyMap.values
      .map {
        var orbitCount = 0
        var body = it

        while (body.orbitedBody != null) {
          orbitCount++
          body = body.orbitedBody!!
        }

        orbitCount
      }
      .sum()
      .run { println("Answer 1:\n$this") }


  getBody("YOU").allOrbitedBodies().intersect(getBody("SAN").allOrbitedBodies())
      .map { getBody("YOU").orbitDegree(it) + getBody("SAN").orbitDegree(it) }
      .min().run { println("Answer 2:\n$this") }
}

private val bodyMap = mutableMapOf<String, Body>()

private fun getBody(name: String) =
    bodyMap[name] ?: Body(name).also { bodyMap[name] = it }

class Body(val name: String, var orbitedBody: Body? = null) {

  fun allOrbitedBodies(): List<Body> {
    val allBodies = mutableListOf<Body>()
    var body = orbitedBody

    while (body != null) {
      allBodies.add(body)
      body = body.orbitedBody
    }

    return allBodies
  }

  fun orbitDegree(otherBody: Body): Int {

    if (orbitedBody == null)
      throw IllegalStateException()

    var orbitCount = 0
    var body = orbitedBody

    while (body != otherBody) {

      if (body?.orbitedBody == null)
        throw IllegalStateException()

      orbitCount++
      body = body.orbitedBody
    }

    return orbitCount
  }

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is Body -> name == other.name
      else -> false
    }
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }
}

private fun readData() {

  Day1::class.java.classLoader.getResource("day6/input_puzzle_1")
      ?.readText()
      ?.lines()
      ?.forEach { readBody(it) }
}

fun readBody(bodyString: String) {

  require(bodyString.matches(Regex("[A-Z0-9]{3}\\)[A-Z0-9]{3}"))) { "$bodyString is not a valid orbit info" }

  bodyString.split(")")
      .run {
        getBody(this[1]).orbitedBody = getBody(this[0])
      }
}

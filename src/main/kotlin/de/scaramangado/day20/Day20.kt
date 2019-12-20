package de.scaramangado.day20

import de.scaramangado.day1.Day1
import de.scaramangado.day13.uniqueResult

fun main() {

  originalInput
      .toNodes()
      .let {
        val start = it.filter { tile -> tile.t.type == Type.EXIT }.uniqueResult() ?: throw NullPointerException()
        shortestPathLength(it, start) { this.t.type == Type.ENTRANCE }
      }
      .run { println("Answer 1:\n$this") }
}

private val testInput = Input("""
                   A               
                   A               
  #################.#############  
  #.#...#...................#.#.#  
  #.#.#.###.###.###.#########.#.#  
  #.#.#.......#...#.....#.#.#...#  
  #.#########.###.#####.#.#.###.#  
  #.............#.#.....#.......#  
  ###.###########.###.#####.#.#.#  
  #.....#        A   C    #.#.#.#  
  #######        S   P    #####.#  
  #.#...#                 #......VT
  #.#.#.#                 #.#####  
  #...#.#               YN....#.#  
  #.###.#                 #####.#  
DI....#.#                 #.....#  
  #####.#                 #.###.#  
ZZ......#               QG....#..AS
  ###.###                 #######  
JO..#.#.#                 #.....#  
  #.#.#.#                 ###.#.#  
  #...#..DI             BU....#..LF
  #####.#                 #.#####  
YN......#               VT..#....QG
  #.###.#                 #.###.#  
  #.#...#                 #.....#  
  ###.###    J L     J    #.#.###  
  #.....#    O F     P    #.#...#  
  #.###.#####.#.#####.#####.###.#  
  #...#.#.#...#.....#.....#.#...#  
  #.#####.###.###.#.#.#########.#  
  #...#.#.....#...#.#.#.#.....#.#  
  #.###.#####.###.###.#.#.#######  
  #.#.........#...#.............#  
  #########.###.###.#############  
           B   J   C               
           U   P   P               
""".trimIndent().lines().map { it.toCharArray().toList() }).parse()

private val originalInput by lazy {
  Input(Day1::class.java.classLoader.getResource("day20/input_puzzle_1")
            ?.readText()
            ?.lines()
            ?.map { it.toCharArray().toList() }
            ?: emptyList()).parse()
}

private data class Position(val x: Int, val y: Int) {
  infix fun leftOf(other: Position) = x < other.x
  infix fun rightOf(other: Position) = x > other.x
  infix fun below(other: Position) = y > other.y
  infix fun above(other: Position) = y < other.y
}

private enum class Type {
  WALL, FLOOR, SPACE, PORTAL, ENTRANCE, EXIT
}

private data class Tile(val position: Position, val type: Type, val portalCode: String? = null)

fun <T> shortestPathLength(nodes: Collection<Node<T>>, goalNode: Node<T>,
                           uniqueStartProperty: Node<T>.() -> Boolean): Int {

  require(goalNode in nodes)

  val start = nodes.filter { it.uniqueStartProperty() }.uniqueResult() ?: throw IllegalStateException()
  start.distance = 0

  val unvisited = nodes.toMutableSet()
  val visited = mutableSetOf<Node<T>>()

  while (unvisited.isNotEmpty()) {
    val node = unvisited.minBy { it.distance } ?: throw IllegalStateException()
    val nextDistance = node.distance + 1
    node.neighbors
        .mapNotNull {
          nodes.filter { n -> n.t == it }.uniqueResult()
        }
        .forEach {
          if (it.distance > nextDistance) {
            it.distance = nextDistance
          }
        }

    visited.add(node)
    unvisited.remove(node)
  }

  return goalNode.distance
}

class Node<T>(val t: T, var distance: Int, val neighbors: Set<T>)

private fun Set<Tile>.toNodes(): Set<Node<Tile>> {

  val validTiles = this.filter { it.type in setOf(Type.FLOOR, Type.PORTAL, Type.ENTRANCE, Type.EXIT) }.toSet()

  return validTiles.map {
    val neighbors = mutableSetOf<Tile>()

    if (it.type == Type.PORTAL) {
      neighbors.add((validTiles - it)
                        .filter { other -> other.type == Type.PORTAL && other.portalCode == it.portalCode }
                        .uniqueResult() ?: throw IllegalStateException(""))
    }

    it.position
        .let { p ->
          setOf(Position(p.x + 1, p.y), Position(p.x - 1, p.y),
                Position(p.x, p.y + 1), Position(p.x, p.y - 1))
        }.mapNotNull { p ->
          validTiles.lastOrNull { t -> t.position == p }
        }
        .forEach { t -> neighbors.add(t) }

    Node(it, Int.MAX_VALUE, neighbors)
  }.toSet()
}

private class Input(private val input: List<List<Char>>) {

  val width = input.map { it.size }.max() ?: 0
  val height = input.size

  fun neighbors(position: Position) =
      position
          .let {
            setOf(Position(it.x + 1, it.y), Position(it.x - 1, it.y),
                  Position(it.x, it.y + 1), Position(it.x, it.y - 1))
          }
          .map { it to getCharacterAtPosition(it) }
          .filter { it.second != null }
          .map { it.first to it.second!! }
          .toSet()

  fun getCharacterAtPosition(position: Position) = input.getOrNull(position.y)?.getOrNull(position.x)
}

private fun Input.parse(): Set<Tile> {

  val tiles = mutableSetOf<Tile>()

  fun generateFloorOrPortal(position: Position): Tile {

    val neighbors = neighbors(position)
    val potentialPortal = neighbors.filter { it.second !in setOf(' ', '.', '#') }.uniqueResult()
        ?: return Tile(position, Type.FLOOR)

    val char = potentialPortal.second.toString()

    val portalCode = with(potentialPortal.first) {
      when {
        this leftOf position -> getCharacterAtPosition(Position(this.x - 1, this.y)).toString() + char
        this rightOf position -> char + getCharacterAtPosition(Position(this.x + 1, this.y))
        this below position -> char + getCharacterAtPosition(Position(this.x, this.y + 1))
        this above position -> getCharacterAtPosition(Position(this.x, this.y - 1)).toString() + char

        else -> throw IllegalStateException()
      }
    }

    val type = when (portalCode) {
      "AA" -> Type.ENTRANCE
      "ZZ" -> Type.EXIT
      else -> Type.PORTAL
    }

    return Tile(position, type, portalCode)
  }

  for (y in 0..height) {
    for (x in 0..width) {
      Position(x, y)
          .let {
            it to when (getCharacterAtPosition(it)) {
              null -> Tile(it, Type.SPACE)
              ' ' -> Tile(it, Type.SPACE)
              '#' -> Tile(it, Type.WALL)
              in 'A'..'Z' -> Tile(it, Type.SPACE)
              '.' -> generateFloorOrPortal(it)
              else -> throw IllegalArgumentException("${getCharacterAtPosition(it)} is not a valid tile type")
            }
          }
          .run { tiles.add(this.second) }
    }
  }

  return tiles.toSet()
}

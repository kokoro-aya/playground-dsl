package org.ironica.playground

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ironica.playground.Direction.*
enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

@Serializable
sealed class Block

@Serializable
@SerialName("OPEN")
object Open : Block()

@Serializable
@SerialName("BLOCKED")
object Blocked : Block()

@Serializable
@SerialName("GEM")
object Gem : Block()

@Serializable
@SerialName("BEEPER")
data class Beeper(val energy: Int) : Block()

@Serializable
@SerialName("CLOSEDSWITCH")
object ClosedSwitch : Block()

@Serializable
@SerialName("OPENEDSWITCH")
object OpenedSwitch : Block()

@Serializable
@SerialName("PORTAL")
data class Portal(val dest: SerializedCoordinate) : Block()


typealias Grid = Array<Array<Block>>

data class Coordinate(var x: Int, var y: Int) {
    fun incrementX() { x += 1 }
    fun decrementX() { x -= 1 }
    fun incrementY() { y += 1 }
    fun decrementY() { y -= 1 }
}

data class Player(val coo: Coordinate, var dir: Direction) {

    lateinit var grid: Grid

    var collectedGem = 0

    private fun isBlockedYPlus() = coo.y < 1 || grid[coo.y - 1][coo.x] == Blocked
    private fun isBlockedYMinus() = coo.y > grid.size - 2 || grid[coo.y + 1][coo.x] == Blocked
    private fun isBlockedXMinus() = coo.x < 1 || grid[coo.y][coo.x - 1] == Blocked
    private fun isBlockedXPlus() = coo.x > grid[0].size - 2 || grid[coo.y][coo.x + 1] == Blocked

    val isOnGem = { grid[coo.y][coo.x] == Gem }
    val isOnOpenedSwitch = { grid[coo.y][coo.x] == OpenedSwitch }
    val isOnClosedSwitch = { grid[coo.y][coo.x] == ClosedSwitch }
    val isBlocked = { when (dir) {
        UP -> isBlockedYPlus()
        DOWN -> isBlockedYMinus()
        LEFT -> isBlockedXMinus()
        RIGHT -> isBlockedXPlus()
    }}
    val isBlockedLeft = { when (dir) {
        RIGHT ->isBlockedYPlus()
        LEFT -> isBlockedYMinus()
        UP -> isBlockedXMinus()
        DOWN -> isBlockedXPlus()
    }}
    val isBlockedRight = { when (dir) {
        LEFT -> isBlockedYPlus()
        RIGHT -> isBlockedYMinus()
        DOWN -> isBlockedXMinus()
        UP -> isBlockedXPlus()
    }}

    fun turnLeft() { dir = when(dir) {
        UP -> LEFT
        LEFT -> DOWN
        DOWN -> RIGHT
        RIGHT -> UP
    }}
    fun moveForward(): Boolean {
        if (!isBlocked()) {
            when (dir) {
                UP -> coo.decrementY()
                LEFT -> coo.decrementX()
                DOWN -> coo.incrementY()
                RIGHT -> coo.incrementX()
            }
            return true
        }
        return false
    }
    fun collectGem(): Boolean {
        if (isOnGem()) {
            collectedGem += 1
            grid[coo.y][coo.x] = Open
            return true
        }
        return false
    }
    fun toggleSwitch(): Boolean {
        if (isOnOpenedSwitch()) {
            grid[coo.y][coo.x] = ClosedSwitch
            return true
        }
        if (isOnClosedSwitch()) {
            grid[coo.y][coo.x] = OpenedSwitch
            return true
        }
        return false
    }
}

class Playground(val grid: Grid, val player: Player, private val initialGem: Int) {

    init {
        player.grid = grid
    }

    fun win(): Boolean {
        return grid.flatMap { it.filter { it == Gem } }.isEmpty() && grid.flatMap { it.filter { it == ClosedSwitch } }.isEmpty()
    }
    fun gemCount(): Int {
        return initialGem - grid.flatMap { it.filter { it == Gem } }.size
    }
    fun switchCount(): Int {
        return grid.flatMap { it.filter { it == OpenedSwitch } }.size
    }


    fun printGrid() {
        grid.forEachIndexed { i, row -> row.forEachIndexed { j, _ ->
                if (player.coo.x == j && player.coo.y == i) {
                    print(when (player.dir) {
                        UP -> "U"
                        DOWN -> "D"
                        LEFT -> "L"
                        RIGHT -> "R"
                    })
                }
                else {
                    print(when (grid[i][j]) {
                        Open -> "_"
                        Blocked -> "B"
                        Gem -> "G"
                        ClosedSwitch -> "X"
                        OpenedSwitch -> "O"
                        is Beeper -> "P"
                        is Portal -> "+"
                    })
            } }
            println()
        }
        println()
    }
}

fun main() {
    val grid = arrayOf(
            arrayOf(Open, ClosedSwitch, Blocked, Blocked, Blocked),
            arrayOf(ClosedSwitch, Open, Open, Open, Blocked),
            arrayOf(Blocked, Gem, Blocked, Gem, Blocked)
    )
    val player = Player(
        Coordinate(0, 0),
        RIGHT
    )

    val playground = Playground(grid, player, 2)
    playground.printGrid()

    player.moveForward()
    if (player.moveForward()) println("moved forward") else println("cannot move forward!")
    if (player.toggleSwitch()) println("toggled switch")
    player.turnLeft(); player.turnLeft(); player.turnLeft()
    playground.printGrid()
    player.moveForward(); player.moveForward()
    if (player.collectGem()) println("Collected gem")
    player.turnLeft(); player.turnLeft()
    playground.printGrid()
    player.moveForward(); player.turnLeft(); player.moveForward()
    if (player.toggleSwitch()) println("toggled switch")
    player.turnLeft(); player.turnLeft()
    playground.printGrid()
    player.moveForward()
    playground.printGrid()

    player.moveForward()
    playground.printGrid()
    player.moveForward()
    player.turnLeft(); player.turnLeft(); player.turnLeft()
    player.moveForward()

    if (player.collectGem()) println("Collected gem")
    player.turnLeft(); player.turnLeft(); player.moveForward()

    playground.printGrid()

    println(playground.win())
    println(playground.gemCount())
    println(playground.switchCount())

}


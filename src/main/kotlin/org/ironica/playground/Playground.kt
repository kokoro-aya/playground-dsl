package org.ironica.playground

import kotlinx.serialization.Serializable
import org.ironica.playground.Direction.*
import org.ironica.playground.Block.*
enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

enum class Block {
    OPEN, BLOCKED, GEM, CLOSEDSWITCH, OPENEDSWITCH
}

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

    private fun isBlockedYPlus() = coo.y < 1 || grid[coo.y - 1][coo.x] == BLOCKED
    private fun isBlockedYMinus() = coo.y > grid.size - 2 || grid[coo.y + 1][coo.x] == BLOCKED
    private fun isBlockedXMinus() = coo.x < 1 || grid[coo.y][coo.x - 1] == BLOCKED
    private fun isBlockedXPlus() = coo.x > grid[0].size - 2 || grid[coo.y][coo.x + 1] == BLOCKED

    val isOnGem = { grid[coo.y][coo.x] == GEM }
    val isOnOpenedSwitch = { grid[coo.y][coo.x] == OPENEDSWITCH }
    val isOnClosedSwitch = { grid[coo.y][coo.x] == CLOSEDSWITCH }
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
            grid[coo.y][coo.x] = OPEN
            return true
        }
        return false
    }
    fun toggleSwitch(): Boolean {
        if (isOnOpenedSwitch()) {
            grid[coo.y][coo.x] = CLOSEDSWITCH
            return true
        }
        if (isOnClosedSwitch()) {
            grid[coo.y][coo.x] = OPENEDSWITCH
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
        return grid.flatMap { it.filter { it == GEM } }.isEmpty() && grid.flatMap { it.filter { it == CLOSEDSWITCH } }.isEmpty()
    }
    fun gemCount(): Int {
        return initialGem - grid.flatMap { it.filter { it == GEM } }.size
    }
    fun switchCount(): Int {
        return grid.flatMap { it.filter { it == OPENEDSWITCH } }.size
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
                        OPEN -> "_"
                        BLOCKED -> "B"
                        GEM -> "G"
                        CLOSEDSWITCH -> "X"
                        OPENEDSWITCH -> "O"
                    })
            } }
            println()
        }
        println()
    }
}

fun main() {
    val grid = arrayOf(
            arrayOf(OPEN, CLOSEDSWITCH, BLOCKED, BLOCKED, BLOCKED),
            arrayOf(CLOSEDSWITCH, OPEN, OPEN, OPEN, BLOCKED),
            arrayOf(BLOCKED, GEM, BLOCKED, GEM, BLOCKED)
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


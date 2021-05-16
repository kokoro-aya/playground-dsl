package org.ironica.playground

import javax.script.ScriptEngineManager

fun main() {

    val grid = arrayOf(
        arrayOf(Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN),
        arrayOf(Block.BLOCKED, Block.GEM, Block.BLOCKED, Block.GEM, Block.BLOCKED, Block.GEM, Block.BLOCKED, Block.GEM, Block.BLOCKED)
    )

    val player = Player(
        Coordinate(0, 0),
        Direction.RIGHT
    )

    val manager = PlaygroundManager(Playground(grid, player, 4))

    val win = play(manager) {
        fun turnRight() {
            for (i in 1..3) turnLeft()
        }

        fun turnBack() {
            for (i in 1..2) turnLeft()
        }

        print("foo")

        for (x in 1..4) {
            print("bar", "foo", "bar")
            moveForward()
            toggleSwitch()
            turnRight()
            moveForward()
            collectGem()
            turnBack()
            moveForward()
            turnRight()
            moveForward()
        }
    }.end()
    println(win)
}
package org.ironica.playground

import kotlinx.coroutines.runBlocking

fun main() {

    val grid = arrayOf(
        arrayOf(
            Open,
            ClosedSwitch,
            Open,
            ClosedSwitch,
            Open,
            ClosedSwitch,
            Open,
            ClosedSwitch,
            Open
        ),
        arrayOf(
            Blocked,
            Gem,
            Blocked,
            Gem,
            Blocked,
            Gem,
            Blocked,
            Gem,
            Blocked
        )
    )

    val players = listOf(Player(
        Coordinate(0, 0),
        Direction.RIGHT
    ))

    val manager = PlaygroundManager(Playground(grid, players, 4, 9999))

    runBlocking {
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
}
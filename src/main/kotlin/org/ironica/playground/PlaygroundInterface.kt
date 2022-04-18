package org.ironica.playground

import kotlinx.serialization.Serializable
import org.ironica.simula.SimulaPoet
import org.ironica.simula.SimulaRunner
import org.ironica.simula.wrapCode

@Serializable
data class Data(val code: String, val grid: List<List<Block>>, val players: List<NamedPlayer>)

@Serializable
data class NamedPlayer(val name: String, val p: SerializedPlayer)

fun calculateInitialGem(grid: Grid): Int = grid.flatten().filter { it == Gem }.size

class PlaygroundInterface(val code: String, val grid: Grid, val players: Array<NamedPlayer>) {

    //    private val player = Player(
//        Coordinate(0, 0),
//        Direction.RIGHT
//    )
//    private val player = Player(
//            Coordinate(5, 2),
//            Direction.DOWN
//        )
    fun start(): Status {
        val codeGen = StringBuilder()
        val sim = SimulaPoet()
        sim.feed(grid)
            .feed(players)
            .feed(calculateInitialGem(grid))
            .generate(codeGen)
        codeGen.append("\n")
        codeGen.append(code.wrapCode().trimIndent())

        val gen = codeGen.toString()

//        println(gen)

        SimulaRunner().evalSnippet(gen).let {
            println(it.first)
            return it.second
        }
    }
}
package org.ironica.playground

import kotlinx.serialization.Serializable
import org.ironica.simula.SimulaPoet
import org.ironica.simula.SimulaRunner
import org.ironica.simula.wrapCode

@Serializable
data class Data(val code: String, val grid: List<List<Block>>, val players: List<NamedPlayer>, val energy: Int)

@Serializable
data class NamedPlayer(val name: String, val p: SerializedPlayer)

fun computeInitialGem(grid: Grid): Int = grid.flatten().filter { it == Gem }.size

// This class cannot simplify to a function
class PlaygroundInterface(
    val code: String, val grid: Grid, val players: Array<NamedPlayer>, val energy: Int) {

    fun start(): Status {
        val codeGen = StringBuilder()
        val sim = SimulaPoet()
        sim.feed(grid)
            .feed(players)
            .feed(computeInitialGem(grid), energy)
            .generate(codeGen)
        codeGen.append("\n")
        codeGen.append(code.wrapCode().trimIndent())

        val gen = codeGen.toString()

        SimulaRunner().evalSnippet(gen).let {
            println(it.first)
            return it.second
        }
    }
}
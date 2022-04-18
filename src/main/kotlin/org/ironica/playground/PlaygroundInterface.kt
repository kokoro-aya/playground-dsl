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

    fun start(): Pair<Any?, CodeStatus> {
        val codeGen = StringBuilder()
        val sim = SimulaPoet()
        sim.feed(grid)
            .feed(players)
            .feed(computeInitialGem(grid), energy)
            .generate(codeGen)
        codeGen.append("\n")
        codeGen.append(code.wrapCode().trimIndent())

        val gen = codeGen.toString()

        println("Processing request, please wait...")

        SimulaRunner().evalSnippet(gen).let {
//            println(it.first)

            println()

            println(when (it.second) {
                CodeStatus.OK -> "[1]Code executed successfully"
                CodeStatus.ERROR -> "[2]Some error occurred while executing your code"
                CodeStatus.INCOMPLETE -> "[3]The code is not complete"
            })

            return it
        }
    }
}
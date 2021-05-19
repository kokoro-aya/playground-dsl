package org.ironica.playground

import kotlinx.serialization.Serializable
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.ironica.simula.ProvidedProperty
import org.ironica.simula.SimulaPoet
import org.ironica.simula.SimulaRunner
import org.ironica.simula.wrapCode
import playgroundGrammarLexer
import playgroundGrammarParser
import kotlin.script.experimental.api.onFailure
import kotlin.script.experimental.api.onSuccess
import kotlin.script.experimental.host.toScriptSource

@Serializable
data class Data(val code: String, val grid: List<List<String>>)

fun convertJsonToGrid(array: List<List<String>>): Grid {
    return array.map { it.map { when (it) {
        "OPEN" -> Block.OPEN
        "BLOCKED" -> Block.BLOCKED
        "GEM" -> Block.GEM
        "OPENEDSWITCH" -> Block.OPENEDSWITCH
        "CLOSEDSWITCH" -> Block.CLOSEDSWITCH
        else -> throw Exception("Cannot parse data to grid")
    } }.toTypedArray() }.toTypedArray()
}

fun calculateInitialGem(grid: Grid): Int = grid.flatten().filter { it == Block.GEM }.size

class PlaygroundInterface(val code: String, val grid: Grid) {
    // TODO: send player coo from front-end
    private val player = Player(
        Coordinate(0, 0),
        Direction.RIGHT
    )

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
            .feed(player)
            .feed(calculateInitialGem(grid))
            .generate(codeGen)
        codeGen.append("\n")
        codeGen.append(code.wrapCode().trimIndent())

        val gen = codeGen.toString()

//        println(gen)

        SimulaRunner.evalSnippet(gen).let {
            println(it.first)
            return it.second
        }
    }
}

class OldInterface(code: String, grid: Grid) {

    private val input: CharStream = CharStreams.fromString(code)
    private val lexer = playgroundGrammarLexer(input)
    private val tokens = CommonTokenStream(lexer)
    private val parser = playgroundGrammarParser(tokens)
    private val tree: ParseTree = parser.top_level()
    // TODO: send player coo from front-end
    private val player = Player(
        Coordinate(0, 0),
        Direction.RIGHT
    )
    //    private val player = Player(
//        Coordinate(0, 0),
//        Direction.RIGHT
//    )
//    private val player = Player(
//            Coordinate(5, 2),
//            Direction.DOWN
//        )
    val playground = Playground(grid, player, calculateInitialGem(grid))
    private val manager = PlaygroundManager(playground)
    private val exec = PlaygroundVisitor(manager)
    fun start() {
        exec.visit(tree)
    }
}
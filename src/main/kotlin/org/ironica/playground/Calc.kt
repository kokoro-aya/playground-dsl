package org.ironica.playground

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams.fromStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import playgroundGrammarLexer
import playgroundGrammarParser
import java.io.FileInputStream
import java.io.InputStream

fun main(args: Array<String>) {
    val inputstream: InputStream = if (args.isNotEmpty()) FileInputStream(args[0]) else System.`in`
    val input: CharStream = fromStream(inputstream)
    val lexer = playgroundGrammarLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = playgroundGrammarParser(tokens)
    val tree: ParseTree = parser.top_level()

    val grid = arrayOf(
        arrayOf(Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN),
        arrayOf(Block.BLOCKED, Block.GEM, Block.BLOCKED, Block.GEM, Block.BLOCKED, Block.GEM, Block.BLOCKED, Block.GEM, Block.BLOCKED)
    )
    val player = Player(
        Coordinate(0, 0),
        Direction.RIGHT
    )
    val playground = Playground(grid, player, 2)
    val manager = PlaygroundManager(playground)
    val exec = PlaygroundVisitor(manager)
    exec.visit(tree)
    playground.printGrid()
    println( if (playground.win()) "stage cleared!" else "not finished...")
}
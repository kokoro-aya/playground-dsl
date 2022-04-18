package org.ironica.simula

import com.squareup.kotlinpoet.*
import org.ironica.playground.*
import kotlin.script.experimental.host.toScriptSource

//fun main() {
//    val out = StringBuilder()
//    val greeterClass = ClassName("", "Greeter")
//    val file = FileSpec.builder("", "HelloWorld")
//        .addType(TypeSpec.classBuilder("Greeter")
//            .primaryConstructor(FunSpec.constructorBuilder()
//                .addParameter("name", String::class)
//                .build())
//            .addProperty(PropertySpec.builder("name", String::class)
//                .initializer("name")
//                .build())
//            .addFunction(FunSpec.builder("greet")
//                .addStatement("println(%P)", "Hello, \$name")
//                .build())
//            .build())
//        .addFunction(FunSpec.builder("main")
//            .addParameter("args", String::class, KModifier.VARARG)
//            .addStatement("%T(args[0]).greet()", greeterClass)
//            .build())
//        .build()
//
//    file.writeTo(out)
//    println(out.toString())
//}


class SimulaPoet {
    private val fs = FileSpec.builder("Simula", "Poet")

    fun feed(grid: Grid): SimulaPoet {
        fs.addProperty(PropertySpec.builder("grid", Grid::class.java)
            .initializer(grid.generateTemplate())
            .build()
        )
        return this
    }
    fun feed(player: Player): SimulaPoet {
        fs.addProperty(PropertySpec.builder("player", Player::class)
            .initializer(player.generateTemplate())
            .build()
        )
        return this
    }

    fun feed(initialGem: Int): SimulaPoet {
        fs.addProperty(PropertySpec.builder("manager", PlaygroundManager::class)
            .initializer("PlaygroundManager(Playground(grid, player, $initialGem))")
            .build()
        )
        return this
    }

    private fun Grid.generateTemplate(): String {
        return ("arrayOf(" + this.map { "\n    arrayOf(" + it.map {
            when (it) {
                Block.OPEN -> "Block.OPEN"
                Block.BLOCKED -> "Block.BLOCKED"
                Block.GEM -> "Block.GEM"
                Block.CLOSEDSWITCH -> "Block.CLOSEDSWITCH"
                Block.OPENEDSWITCH -> "Block.OPENEDSWITCH"
            }
        } + ")" } + "\n)").replace("[", "").replace("]", "")
    }

    private fun Player.generateTemplate(): String {
        return """
            Player(
                Coordinate(${this.coo.x}, ${this.coo.y}),
                Direction.${this.dir.name}
            )""".trimIndent()
    }

    fun generate(out: Appendable) {
        fs.build().writeTo(out)
    }
}

fun String.wrapCode(): String {
    return "runBlocking {\n" + "    val _win = play(manager) {\n" + this.split("\n").joinToString("\n") { "        $it" } + "\n}.end()\n" + "    println(_win)\n" + "}\n"
}

fun main() {
    val grid = arrayOf(
        arrayOf(Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN, Block.CLOSEDSWITCH, Block.OPEN),
        arrayOf(Block.BLOCKED, Block.GEM, Block.BLOCKED, Block.GEM, Block.BLOCKED, Block.GEM, Block.BLOCKED, Block.GEM, Block.BLOCKED)
    )

    val player = Player(
        Coordinate(0, 0),
        Direction.RIGHT
    )

    val code = """
        fun turnRight() {
            for (i in 1..3) turnLeft()
        }

        fun turnBack() {
            for (i in 1..2) turnLeft()
        }
        
        println("Player comparison: " + (Player(Coordinate(0, 0), Direction.RIGHT) == Player(Coordinate(0, 0), Direction.RIGHT)))

        for (x in 1..4) {
            moveForward()
            toggleSwitch()
            launch {
                delay(500)
                println("Foo")
            }
            turnRight()
            moveForward()
            collectGem()
            launch {
                delay(750)
                println("Bar")
            }
            turnBack()
            moveForward()
            turnRight()
            moveForward()
        }
    """.wrapCode().trimIndent()

    val codeGen = StringBuilder()

    val sim = SimulaPoet()
    sim
        .feed(grid)
        .feed(player)
        .feed(2)
        .generate(codeGen)

    codeGen.append("\n")
    codeGen.append(code)

    val gen = codeGen.toString()

    println(gen)

    println(SimulaRunner().evalSnippet(gen).first)

    println(SimulaRunner().evalSnippet(gen).first)

}


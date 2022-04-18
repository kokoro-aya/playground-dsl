package org.ironica.playground

import kotlinx.serialization.Serializable

var payloadStorage: ThreadLocal<MutableList<Payload>> = ThreadLocal.withInitial(::mutableListOf)

var statusStorage: ThreadLocal<GameStatus> = ThreadLocal.withInitial { GameStatus.PENDING }

fun returnedPayloads(): List<Payload> {
    return payloadStorage.get().map { it }
}

@Serializable
data class SerializedPlayer(val x: Int, val y: Int, val dir: Direction)

@Serializable
data class SerializedCoordinate(val x: Int, val y: Int)

@Serializable
data class SerializedGrid(val grid: Grid)

@Serializable
data class Payload(val player: List<SerializedPlayer>, val grid: SerializedGrid, val consoleLog: String, val special: String)

var gameCodeStatus: CodeStatus? = null

enum class CodeStatus { OK, ERROR, INCOMPLETE }

@Serializable
sealed class Message
@Serializable
data class NormalMessage(val codeStatus: CodeStatus, val payload: List<Payload>, val gameStatus: GameStatus): Message()
@Serializable
data class ErrorMessage(val codeStatus: CodeStatus, val msg: String): Message()
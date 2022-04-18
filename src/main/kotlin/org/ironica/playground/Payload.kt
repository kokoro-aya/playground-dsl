package org.ironica.playground

import kotlinx.serialization.Serializable

var payloadStorage: ThreadLocal<MutableList<Payload>> = ThreadLocal.withInitial(::mutableListOf)

@Serializable
data class SerializedPlayer(val x: Int, val y: Int, val dir: Direction)

@Serializable
data class SerializedCoordinate(val x: Int, val y: Int)

@Serializable
data class SerializedGrid(val grid: Grid)

@Serializable
data class Payload(val player: List<SerializedPlayer>, val grid: SerializedGrid, val consoleLog: String, val special: String)

var gameStatus: Status? = null

enum class Status { OK, ERROR, INCOMPLETE }

@Serializable
sealed class Message
@Serializable
data class NormalMessage(val status: Status, val payload: List<Payload>): Message()
@Serializable
data class ErrorMessage(val status: Status, val msg: String): Message()
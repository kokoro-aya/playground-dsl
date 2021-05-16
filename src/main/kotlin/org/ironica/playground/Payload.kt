package org.ironica.playground

import kotlinx.serialization.Serializable

@Serializable
data class SerializedPlayer(val x: Int, val y: Int, val dir: Direction)

@Serializable
data class SerializedGrid(val grid: Grid)

@Serializable
data class Payload(val player: SerializedPlayer, val grid: SerializedGrid, val consoleLog: String, val special: String)


val payloadStorage = mutableListOf<Payload>()
var gameStatus: Status? = null

enum class Status { OK, ERROR }

@Serializable
sealed class Message
@Serializable
data class NormalMessage(val status: Status, val payload: List<Payload>): Message()
@Serializable
data class ErrorMessage(val status: Status, val msg: String): Message()
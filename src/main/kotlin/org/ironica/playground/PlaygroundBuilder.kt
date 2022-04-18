package org.ironica.playground

class Console(val manager: PlaygroundManager) {
    fun <T> log(vararg strings: T) {
        return manager.print(strings.map { it.toString() })
    }
}

class PlaygroundBuilder(private val manager: PlaygroundManager) {

    val console = Console(manager)

    val isOnGem: Boolean
        get() = manager.isOnGem.invoke()
    val isOnOpenedSwitch: Boolean
        get() = manager.isOnOpenedSwitch.invoke()
    val isOnClosedSwitch: Boolean
        get() = manager.isOnClosedSwitch.invoke()
    val isBlocked: Boolean
        get() = manager.isBlocked.invoke()
    val isBlockedLeft: Boolean
        get() = manager.isBlockedLeft.invoke()
    val isBlockedRight: Boolean
        get() = manager.isBlockedRight.invoke()
    val collectedGem: Int
        get() = manager.collectedGem()

    val gemCount: Int
        get() = manager.gemCount()
    val switchCount: Int
        get() = manager.switchCount()

    fun turnLeft() = manager.turnLeft()
    fun moveForward() = manager.moveForward()
    fun collectGem() = manager.collectGem()
    fun toggleSwitch() = manager.toggleSwitch()
    fun <T> print(vararg strings: T) = console.log(*strings)

    fun end(): Boolean {
        return manager.win()
    }
}

fun play(manager: PlaygroundManager, initializer: PlaygroundBuilder.() -> Unit): PlaygroundBuilder {
    return PlaygroundBuilder(manager).apply(initializer)
}
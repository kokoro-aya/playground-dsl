package org.ironica.playground

class PlaygroundManager(val playground: Playground) {
    // Decorator pattern

    private var consoleLog = ""
    private var special = ""

    init {
        appendEntry()
    }

    val isOnGem = playground.player.isOnGem
    val isOnOpenedSwitch = playground.player.isOnOpenedSwitch
    val isOnClosedSwitch = playground.player.isOnClosedSwitch
    val isBlocked = playground.player.isBlocked
    val isBlockedLeft = playground.player.isBlockedLeft
    val isBlockedRight = playground.player.isBlockedRight
    fun collectedGem(): Int {
        return playground.player.collectedGem
    }

    fun turnLeft() {
        playground.player.turnLeft()
//        printGrid()
        appendEntry()
    }
    fun moveForward() {
        playground.player.moveForward()
//        printGrid()
        appendEntry()
    }
    fun collectGem() {
        playground.player.collectGem()
//        printGrid()
        this.special = "GEM"
        appendEntry()
    }
    fun toggleSwitch() {
        playground.player.toggleSwitch()
//        printGrid()
        this.special = "SWITCH"
        appendEntry()
    }

    fun print(lmsg: List<String>) {
        lmsg.forEach { print("$it ") }
        println()
        lmsg.forEach { consoleLog += "$it " }
        consoleLog += "\n"
        appendEntry()
    }

    fun win(): Boolean {
        return playground.win()
    }
    fun gemCount(): Int {
        return playground.gemCount()
    }
    fun switchCount(): Int {
        return playground.switchCount()
    }
    private fun printGrid() {
        return playground.printGrid()
    }

    private fun appendEntry() {
        if (payloadStorage.get().size > 1000)
            throw Exception("Too many entries!")
        val currentGrid: Array<Array<Block>> = Array(playground.grid.size) { Array(playground.grid[0].size) { Open } }
        for (i in playground.grid.indices)
            for (j in playground.grid[0].indices)
                currentGrid[i][j] = playground.grid[i][j]
        val payload = Payload(
            SerializedPlayer(playground.player.coo.x, playground.player.coo.y, playground.player.dir),
            SerializedGrid(currentGrid),
            this.consoleLog,
            this.special
        )
        payloadStorage.get().add(payload)
        this.special = ""
    }

}
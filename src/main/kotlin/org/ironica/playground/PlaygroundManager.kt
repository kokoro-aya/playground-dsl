package org.ironica.playground

class PlaygroundManager(val playground: Playground) {
    // Decorator pattern

    private var consoleLog = ""
    private var special = ""

    init {
        appendEntry()
    }
    
    private fun getDefault(): Player = playground.players.first()

    val isOnGem = getDefault().isOnGem
    val isOnOpenedSwitch = getDefault().isOnOpenedSwitch
    val isOnClosedSwitch = getDefault().isOnClosedSwitch
    val isBlocked = getDefault().isBlocked
    val isBlockedLeft = getDefault().isBlockedLeft
    val isBlockedRight = getDefault().isBlockedRight
    fun collectedGem(): Int {
        return getDefault().collectedGem
    }

    fun turnLeft() {
        getDefault().turnLeft()
//        printGrid()
        appendEntry()
    }
    fun moveForward() {
        getDefault().moveForward()
//        printGrid()
        appendEntry()
    }
    fun collectGem() {
        getDefault().collectGem()
//        printGrid()
        this.special = "GEM"
        appendEntry()
    }
    fun toggleSwitch() {
        getDefault().toggleSwitch()
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
            playground.players.map { SerializedPlayer(it.coo.x, it.coo.y, it.dir) },
            SerializedGrid(currentGrid),
            this.consoleLog,
            this.special
        )
        payloadStorage.get().add(payload)
        this.special = ""
    }

}
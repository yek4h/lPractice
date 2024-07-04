package net.lyragames.practice.queue

import net.lyragames.practice.kit.Kit

class Queue(var kit: Kit, var type: QueueType) {

    val queuePlayers: MutableList<QueuePlayer> = mutableListOf()
    var requiredPlayers: Int = 2

    fun addPlayer(queuePlayer: QueuePlayer) {
        queuePlayers.add(queuePlayer)
    }

    fun removePlayer(queuePlayer: QueuePlayer) {
        queuePlayers.remove(queuePlayer)
    }

    fun getQueueingPlayers(): List<QueuePlayer> {
        return queuePlayers.toList()
    }

    fun tickAllRanges() {
        queuePlayers.forEach { it.tickRange() }
    }

    fun getPlayerCount(): Int {
        return queuePlayers.size
    }
}
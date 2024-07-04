package net.lyragames.practice.utils.scoreboard

import org.bukkit.entity.Player


/*
 * This project can't be redistributed without
 * authorization of the developer
 *
 * Project @ lPractice
 * @author yek4h © 2024
 * Date: 03/07/2024
*/

interface ScoreInterface {
    fun getTitle(player: Player): String
    fun getLines(player: Player): List<String>
}
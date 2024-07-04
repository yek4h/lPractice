package net.lyragames.practice.profile.statistics.global

import java.util.*


/**
 * This Project is property of Zowpy © 2022
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 2/15/2022
 * Project: lPractice
 */

class GlobalStatistics {

    var elo = 1000
    var wins = 0
    var losses = 0

    var bestWinStreak = 0
    var currentWinStreak = 0
    var dailyWinStreak = 0
    var dailyWinStreakStartDate: Date? = null
}
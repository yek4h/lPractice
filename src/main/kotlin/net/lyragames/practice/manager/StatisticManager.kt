package net.lyragames.practice.manager

import net.lyragames.practice.kit.Kit
import net.lyragames.practice.profile.Profile
import net.lyragames.practice.utils.EloUtil

/**
 * This Project is property of Zowpy © 2023
 * Redistribution of this Project is not allowed
 *
 * @author Zowpy
 * Created: 6/7/2023
 * Project: lPractice
 */

object StatisticManager {

    fun win(profile: Profile, loserProfile: Profile, kit: Kit, ranked: Boolean) {
        val globalStatistics = profile.globalStatistic

        globalStatistics.apply {
            wins++
            currentWinStreak++
            if (currentWinStreak >= bestWinStreak) {
                bestWinStreak = currentWinStreak
            }
        }

        profile.getKitStatistic(kit.name)?.let { kitStatistic ->
            kitStatistic.wins++

            if (ranked) {

                kitStatistic.rankedStreak++
                if (kitStatistic.rankedStreak >= kitStatistic.rankedBestStreak) {
                    kitStatistic.rankedBestStreak = kitStatistic.rankedStreak
                }
                kitStatistic.rankedDailyStreak++
                kitStatistic.rankedWins++

                val loserKitStatistic = loserProfile.getKitStatistic(kit.name)

                val (newWinnerElo, newLoserElo) = loserKitStatistic?.let { loserStat ->
                    EloUtil.getNewRating(loserStat.elo, kitStatistic.elo, false) to
                            EloUtil.getNewRating(kitStatistic.elo, loserStat.elo, true)
                } ?: (kitStatistic.elo to kitStatistic.elo)

                kitStatistic.elo = newWinnerElo
                loserKitStatistic?.elo = newLoserElo

                if (kitStatistic.elo >= kitStatistic.peakELO) {
                    kitStatistic.peakELO = kitStatistic.elo
                }

                loserProfile.save()
            }

            kitStatistic.apply {
                currentStreak++
                currentDailyStreak++

                if (!ranked) {
                    currentCasualStreak++

                    if (currentCasualStreak >= bestCasualStreak) {
                        bestCasualStreak = currentCasualStreak
                    }
                }

                if (currentStreak >= bestStreak) {
                    bestStreak = currentStreak
                }

                if (currentDailyStreak >= bestDailyStreak) {
                    bestDailyStreak = currentDailyStreak
                }
            }

            // Actualizar el globalElo
            profile.updateGlobalElo()
        }

        profile.save(true)
    }

    fun loss(profile: Profile, kit: Kit, ranked: Boolean) {
        val globalStatistics = profile.globalStatistic

        globalStatistics.apply {
            losses++
            currentWinStreak = 0
            dailyWinStreak = 0
        }

        profile.getKitStatistic(kit.name)?.let { kitStatistic ->
            kitStatistic.apply {
                losses++
                currentStreak = 0
                currentDailyStreak = 0
                currentCasualStreak = 0
                bestCasualStreak = 0
                bestDailyStreak = 0

                if (ranked) {
                    rankedLosses++

                    rankedBestStreak = 0
                    rankedStreak = 0
                    rankedDailyStreak = 0
                }
            }

            // Actualizar el globalElo
            profile.updateGlobalElo()
        } ?: println("Loser profile is null for $kit")

        profile.save(true)
    }
}
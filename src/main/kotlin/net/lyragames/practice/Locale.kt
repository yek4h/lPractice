package net.lyragames.practice

import net.lyragames.practice.utils.CC

enum class Locale(private val path: String) {

    CANT_DO_THIS("CANT-DO-THIS"),
    CLICK_TO_JOIN("CLICK-TO-JOIN"),
    CLICK_TO_ACCEPT("CLICK-TO-ACCEPT"),

    CANT_DUEL_YOURSELF("DUEL.CANT-DUEL-YOURSELF"),
    ONGOING_DUEL("DUEL.ONGOING-DUEL"),
    BUSY_PLAYER("DUEL.BUSY-PLAYER"),
    DISABLED_DUELS("DUEL.DUEL-DISABLED"),
    INVALID_DUEL("DUEL.INVALID-DUEL"),

    DUEL_REQUEST("DUEL.DUEL-REQUEST"),
    DUEL_REQUEST_FOOTER("DUEL.DUEL-REQUEST-FOOTER"),

    NOT_IN_A_PARTY("PARTY.NOT-IN-A-PARTY"),
    OTHER_NOT_IN_A_PARTY("PARTY.OTHER-NOT-IN-A-PARTY"),

    JOINED_PARTY("PARTY.JOINED-PARTY"),
    CANT_ACCEPT_PARTY_DUEL("PARTY.CANT-ACCEPT-DUEL-REQUEST"),
    ALREADY_IN_PARTY("PARTY.ALREADY-IN-PARTY"),
    CREATED_PARTY("PARTY.CREATED-PARTY"),
    DISBANDED_PARTY("PARTY.DISBANDED-PARTY"),
    LEFT_PARTY("PARTY.LEFT-PARTY"),
    CANT_INVITE_YOURSELF("PARTY.CANT-INVITE-YOURSELF"),
    PLAYER_ALREADY_IN_PARTY("PARTY.PLAYER-ALREADY-IN-PARTY"),
    ALREADY_INVITED_PLAYER("PARTY.ALREADY-INVITED-PLAYER"),
    PARTY_INVITED_MESSAGE("PARTY.INVITED-MESSAGE"),
    JOIN_OWN_PARTY("PARTY.JOIN-OWN-PARTY"),
    ISNT_IN_PARTY("PARTY.ISNT-IN-PARTY"),
    BANNED_FROM_PARTY("PARTY.BANNED-FROM-PARTY"),
    NOT_INVITED("PARTY.NOT-INVITED"),
    PARTY_EXPIRED("PARTY.PARTY-EXPIRED"),
    JOIN_PARTY_BROADCAST("PARTY.JOINED-PARTY-BROADCAST"),

    NO_ACTIVE_EVENTS("EVENT.NO-ACTIVE-EVENTS"),
    EVENT_FULL("EVENT.EVENT-FULL"),
    ALREADY_IN_EVENT("EVENT.ALREADY-IN"),
    ALREADY_STARTED("EVENT.ALREADY-STARTED"),
    NOT_ENOUGH_PLAYER("EVENT.NOT-ENOUGH-PLAYERS"),
    NOT_IN_FFA("FFA.NOT-IN-FFA"),
    LEFT_FFA("FFA.LEFT-FFA"),

    COULDNT_FIND_INVENTORY("INVENTORY.COULDN'T-FIND"),

    STOPPED_SPECTATING("SPECTATE.STOPPED-SPECTATING"),
    STOPPED_SPECTATING_SILENT("SPECTATE.STOPPED-SPECTATING-SILENT"),
    STARTED_SPECTATING("SPECTATE.STARTED-SPECTATING"),
    STARTED_SPECTATING_SILENT("SPECTATE.STARTED-SPECTATING-SILENT"),

    NOT_IN_A_MATCH("SPECTATE.NOT-IN-A-MATCH"),
    SPECTATING_DISABLED("SPECTATE.SPECTATING-DISABLED"),

    PLAYER_DISCONNECTED("MATCH.DISCONNECTED"),
    PLAYER_DIED("MATCH.DIED-NATURALLY"),
    PLAYED_KILLED("MATCH.KILLED-BY-PLAYER"),

    BREAK_OWN_BED("BEDFIGHTS.BREAK-OWN-BED"),
    BED_ALREADY_BROKEN("BEDFIGHTS.BED-ALREADY-BROKEN"),
    BED_DESTROYED("BEDFIGHTS.BED-DESTROYED"),
    BEDFIGHTS_PLAYER_KILLED("BEDFIGHTS.PLAYER-KILLED"),
    FINAL_TAG("BEDFIGHTS.FINAL-TAG"),

    CANT_PLACE("BUILD.CANT-PLACE"),

    FIREBALL_COOLDOWN("COOLDOWN.FIREBALL-COOLDOWN-TIME"),
    ENDERPEARL_COOLDOWN_DONE("COOLDOWN.ENDERPEARL-COOLDOWN-DONE"),
    ENDERPERL_COOLDOWN_TIME("COOLDOWN.ENDERPEARL-COOLDOWN-TIME"),

    ELO_SEARCH("ELO.SEARCH"),

    ALREADY_RATED("RATING.ALREADY-RATED"),
    THANK_YOU("RATING.THANK-YOU"),
    DISABLED_MAP_RATING("RATING.DISABLED-RATING"),

    CANT_FIND_KIT("EXCEPTION.CANT-FIND-KIT"),
    CANT_FIND_ARENA("EXCEPTION.CANT-FIND-ARENA");

    fun getMessage(): String {
        return CC.translate(getNormalMessage())
    }

    fun getNormalMessage(): String {
        return PracticePlugin.instance.languageFile.getString(path)
    }
}
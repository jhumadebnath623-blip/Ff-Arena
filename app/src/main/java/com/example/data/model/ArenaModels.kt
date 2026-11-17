package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TournamentMode(val label: String, val teammatesRequired: Int) {
    SOLO("Solo", 0),
    DUO("Duo", 1),
    SQUAD("Squad", 3);

    companion object {
        fun fromRaw(raw: String): TournamentMode =
            entries.firstOrNull { it.name == raw } ?: SQUAD
    }
}

enum class TournamentCategory(val label: String) {
    BATTLE_ROYALE("Battle Royale"),
    CLASH_SQUAD("Clash Squad");

    companion object {
        fun fromRaw(raw: String): TournamentCategory =
            entries.firstOrNull { it.name == raw } ?: BATTLE_ROYALE
    }
}

enum class TournamentStatus(val label: String) {
    UPCOMING("Upcoming"),
    LIVE("Live"),
    COMPLETED("Completed");

    companion object {
        fun fromRaw(raw: String): TournamentStatus =
            entries.firstOrNull { it.name == raw } ?: UPCOMING
    }
}

enum class BannerKey(val key: String, val label: String) {
    BATTLE_ROYALE("battle_royale", "Battle Royale Arena"),
    CLASH_SQUAD("clash_squad", "Clash Squad Tactical"),
    CHAMPIONSHIP("championship", "Grand Championship");

    companion object {
        fun fromRaw(raw: String): BannerKey =
            entries.firstOrNull { it.key == raw } ?: BATTLE_ROYALE
    }
}

enum class RegistrationStatus(val label: String) {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    companion object {
        fun fromRaw(raw: String): RegistrationStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class WinnerBadge(val label: String) {
    CHAMPION("Champion"),
    RUNNER_UP("Runner-Up"),
    THIRD("3rd Place"),
    NONE("Participant");

    companion object {
        fun fromRaw(raw: String): WinnerBadge =
            entries.firstOrNull { it.name == raw } ?: NONE
    }
}

enum class AnnouncementType(val label: String) {
    NEW_TOURNAMENT("New Tournament"),
    ROOM_READY("Room Ready"),
    MATCH_REMINDER("Match Reminder"),
    MATCH_RESULT("Match Result"),
    WINNER("Winner"),
    GENERAL("Announcement");

    companion object {
        fun fromRaw(raw: String): AnnouncementType =
            entries.firstOrNull { it.name == raw } ?: GENERAL
    }
}

enum class NotificationType(val label: String) {
    REGISTRATION("Registration"),
    APPROVAL("Status Update"),
    ROOM_READY("Room Details"),
    REMINDER("Reminder"),
    RESULT("Match Result"),
    WINNER("Winner");

    companion object {
        fun fromRaw(raw: String): NotificationType =
            entries.firstOrNull { it.name == raw } ?: REGISTRATION
    }
}

@Keep
@IgnoreExtraProperties
data class PublicPlayer(
    @DocumentId val id: String = "",
    val userId: String = "",
    val playerName: String = "",
    val ign: String = "",
    val freeFireUid: String = "",
    val avatarKey: String = "cyber_red",
    val photoUrl: String? = null,
    val totalWins: Long = 0L,
    val totalKills: Long = 0L,
    val matchesPlayed: Long = 0L,
    val totalPoints: Long = 0L,
    val achievements: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PrivatePlayer(
    @DocumentId val id: String = "",
    val userId: String = "",
    val playerName: String = "",
    val ign: String = "",
    val freeFireUid: String = "",
    val whatsappNumber: String = "",
    val email: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminMember(
    @DocumentId val id: String = "",
    val uid: String = "",
    val email: String = "",
    val grantedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Tournament(
    @DocumentId val id: String = "",
    val title: String = "",
    val mode: String = TournamentMode.SQUAD.name,
    val category: String = TournamentCategory.BATTLE_ROYALE.name,
    val status: String = TournamentStatus.UPCOMING.name,
    val bannerKey: String = BannerKey.BATTLE_ROYALE.key,
    val mapName: String = "Bermuda",
    val matchTimeMillis: Long = 0L,
    val roomOpenTimeMillis: Long = 0L,
    val maxSlots: Long = 12L,
    val registeredSlots: Long = 0L,
    val entryType: String = "FREE",
    val prizeDetails: String = "",
    val rulesText: String = "",
    val featured: Boolean = false,
    val winnerName: String? = null,
    val runnerUpName: String? = null,
    val thirdPlaceName: String? = null,
    val mvpName: String? = null,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val modeEnum: TournamentMode get() = TournamentMode.fromRaw(mode)
    val categoryEnum: TournamentCategory get() = TournamentCategory.fromRaw(category)
    val statusEnum: TournamentStatus get() = TournamentStatus.fromRaw(status)
    val bannerEnum: BannerKey get() = BannerKey.fromRaw(bannerKey)
    val availableSlots: Long get() = (maxSlots - registeredSlots).coerceAtLeast(0L)
}

@Keep
@IgnoreExtraProperties
data class RoomAccess(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val roomId: String = "",
    val roomPassword: String = "",
    val notes: String = "",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Registration(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tournamentId: String = "",
    val tournamentTitle: String = "",
    val mode: String = TournamentMode.SQUAD.name,
    val playerName: String = "",
    val ign: String = "",
    val freeFireUid: String = "",
    val whatsappNumber: String = "",
    val teamName: String = "",
    val teammateIgns: List<String> = emptyList(),
    val teammateUids: List<String> = emptyList(),
    val status: String = RegistrationStatus.PENDING.name,
    val adminNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: RegistrationStatus get() = RegistrationStatus.fromRaw(status)
    val modeEnum: TournamentMode get() = TournamentMode.fromRaw(mode)
}

@Keep
@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val tournamentTitle: String = "",
    val userId: String = "",
    val playerName: String = "",
    val ign: String = "",
    val teamName: String = "",
    val freeFireUid: String = "",
    val kills: Long = 0L,
    val placementRank: Long = 1L,
    val positionPoints: Long = 0L,
    val totalPoints: Long = 0L,
    val winnerBadge: String = WinnerBadge.NONE.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val badgeEnum: WinnerBadge get() = WinnerBadge.fromRaw(winnerBadge)
}

@Keep
@IgnoreExtraProperties
data class Announcement(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = AnnouncementType.GENERAL.name,
    val tournamentId: String? = null,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val typeEnum: AnnouncementType get() = AnnouncementType.fromRaw(type)
}

@Keep
@IgnoreExtraProperties
data class UserNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = NotificationType.REGISTRATION.name,
    val tournamentId: String? = null,
    val readFlag: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val typeEnum: NotificationType get() = NotificationType.fromRaw(type)
}

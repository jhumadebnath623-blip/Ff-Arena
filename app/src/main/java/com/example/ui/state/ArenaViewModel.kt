package com.example.ui.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminMember
import com.example.data.model.Announcement
import com.example.data.model.AnnouncementType
import com.example.data.model.LeaderboardEntry
import com.example.data.model.NotificationType
import com.example.data.model.PrivatePlayer
import com.example.data.model.PublicPlayer
import com.example.data.model.Registration
import com.example.data.model.RegistrationStatus
import com.example.data.model.RoomAccess
import com.example.data.model.Tournament
import com.example.data.model.WinnerBadge
import com.example.data.model.UserNotification
import com.example.data.remote.ArenaRepository
import com.example.data.remote.PRIMARY_ADMIN_EMAIL
import com.example.data.remote.unwrapFirestoreMessage
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class UiFeedback(
    val message: String,
    val isError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ArenaViewModel(
    private val repository: ArenaRepository,
    val currentUserId: String,
    val currentUserEmail: String,
    val isEmailVerified: Boolean,
    val googleDisplayName: String,
    val googlePhotoUrl: String?
) : ViewModel() {

    private val _feedback = MutableStateFlow<UiFeedback?>(null)
    val feedback: StateFlow<UiFeedback?> = _feedback.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _selectedTournamentId = MutableStateFlow<String?>(null)

    val currentTimeMillis: StateFlow<Long> = flow {
        while (coroutineContext.isActive) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = System.currentTimeMillis()
    )

    private fun <T> Flow<T>.toUiStateFlow(tag: String, fallback: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error in $tag", rootError)
                emit(UiState.Error(rootError.message ?: fallback))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private fun <T> Flow<T>.toSafeStateFlow(initial: T, tag: String): StateFlow<T> =
        this.catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Stream warning in $tag", firestoreError ?: error)
            emit(initial)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = initial
        )

    val tournamentsState: StateFlow<UiState<List<Tournament>>> =
        repository.observeTournaments()
            .toUiStateFlow("tournaments", "Failed to load tournaments")

    val publicPlayersState: StateFlow<UiState<List<PublicPlayer>>> =
        repository.observePublicPlayers()
            .toUiStateFlow("public_players", "Failed to load players")

    val leaderboardState: StateFlow<UiState<List<LeaderboardEntry>>> =
        repository.observeLeaderboard()
            .toUiStateFlow("leaderboard", "Failed to load leaderboard")

    val myPublicProfile: StateFlow<PublicPlayer?> =
        repository.observePublicPlayer(currentUserId)
            .toSafeStateFlow(null, "myPublicProfile")

    val myPrivateProfile: StateFlow<PrivatePlayer?> =
        repository.observePrivatePlayer(currentUserId)
            .toSafeStateFlow(null, "myPrivateProfile")

    val myRegistrations: StateFlow<List<Registration>> =
        repository.observeMyRegistrations(currentUserId)
            .toSafeStateFlow(emptyList(), "myRegistrations")

    val announcements: StateFlow<List<Announcement>> =
        repository.observeAnnouncements()
            .toSafeStateFlow(emptyList(), "announcements")

    val notifications: StateFlow<List<UserNotification>> =
        repository.observeUserNotifications(currentUserId)
            .toSafeStateFlow(emptyList(), "notifications")

    val unreadNotificationsCount: StateFlow<Int> =
        notifications.map { list -> list.count { !it.readFlag } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = 0
            )

    private val isBootstrappedAdmin: Boolean =
        isEmailVerified && currentUserEmail.equals(PRIMARY_ADMIN_EMAIL, ignoreCase = true)

    val isAdmin: StateFlow<Boolean> = if (isBootstrappedAdmin) {
        MutableStateFlow(true).asStateFlow()
    } else {
        repository.observeIsAdminDoc(currentUserId)
            .toSafeStateFlow(false, "isAdminDoc")
    }

    val allRegistrationsForAdmin: StateFlow<List<Registration>> =
        isAdmin.flatMapLatest { admin ->
            if (admin) repository.observeAllRegistrations() else flowOf(emptyList())
        }.toSafeStateFlow(emptyList(), "allRegistrationsForAdmin")

    val adminsList: StateFlow<List<AdminMember>> =
        isAdmin.flatMapLatest { admin ->
            if (admin) repository.observeAdmins() else flowOf(emptyList())
        }.toSafeStateFlow(emptyList(), "adminsList")

    val selectedRoomAccess: StateFlow<RoomAccess?> =
        combine(_selectedTournamentId, myRegistrations, isAdmin) { tid, regs, admin ->
            if (tid.isNullOrBlank()) {
                null
            } else {
                val approved = regs.any { it.tournamentId == tid && it.statusEnum == RegistrationStatus.APPROVED }
                if (admin || approved) tid else null
            }
        }.flatMapLatest { authorizedTid ->
            if (authorizedTid != null) repository.observeRoomAccess(authorizedTid) else flowOf(null)
        }.toSafeStateFlow(null, "selectedRoomAccess")

    fun selectTournamentForRoom(tournamentId: String?) {
        _selectedTournamentId.value = tournamentId
    }

    fun clearFeedback() {
        _feedback.value = null
    }

    private fun showSuccess(msg: String) {
        _feedback.value = UiFeedback(msg, isError = false)
    }

    private fun showError(msg: String) {
        _feedback.value = UiFeedback(msg, isError = true)
    }

    // --- PLAYER ACTIONS ---

    fun saveProfile(
        playerName: String,
        ign: String,
        freeFireUid: String,
        whatsappNumber: String,
        avatarKey: String,
        photoUrl: String?,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.savePlayerProfile(
                playerName = playerName,
                ign = ign,
                freeFireUid = freeFireUid,
                whatsappNumber = whatsappNumber,
                avatarKey = avatarKey,
                photoUrl = photoUrl,
                existingPublic = myPublicProfile.value,
                existingPrivate = myPrivateProfile.value
            )
            _isBusy.value = false
            res.onSuccess {
                showSuccess("Esports profile saved!")
                onDone()
            }.onFailure {
                showError(it.unwrapFirestoreMessage("Could not save profile"))
            }
        }
    }

    fun registerForTournament(
        tournament: Tournament,
        playerName: String,
        ign: String,
        freeFireUid: String,
        whatsappNumber: String,
        teamName: String,
        teammateIgns: List<String>,
        teammateUids: List<String>,
        onConfirmed: () -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.registerForTournament(
                tournament = tournament,
                playerName = playerName,
                ign = ign,
                freeFireUid = freeFireUid,
                whatsappNumber = whatsappNumber,
                teamName = teamName,
                teammateIgns = teammateIgns,
                teammateUids = teammateUids
            )
            _isBusy.value = false
            res.onSuccess {
                showSuccess("Registered for ${tournament.title}!")
                onConfirmed()
            }.onFailure {
                showError(it.unwrapFirestoreMessage("Registration failed"))
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(currentUserId, notificationId)
        }
    }

    fun markAllNotificationsRead() {
        val unreadIds = notifications.value.filter { !it.readFlag }.map { it.id }
        if (unreadIds.isEmpty()) return
        viewModelScope.launch {
            repository.markAllNotificationsRead(currentUserId, unreadIds)
                .onSuccess { showSuccess("All notifications marked as read") }
        }
    }

    // --- ADMIN ACTIONS ---

    fun createTournament(
        title: String,
        mode: String,
        category: String,
        status: String,
        bannerKey: String,
        mapName: String,
        matchTimeMillis: Long,
        roomOpenTimeMillis: Long,
        maxSlots: Long,
        prizeDetails: String,
        rulesText: String,
        featured: Boolean,
        broadcastAnnouncement: Boolean,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.createTournament(
                title = title,
                mode = mode,
                category = category,
                status = status,
                bannerKey = bannerKey,
                mapName = mapName,
                matchTimeMillis = matchTimeMillis,
                roomOpenTimeMillis = roomOpenTimeMillis,
                maxSlots = maxSlots,
                prizeDetails = prizeDetails,
                rulesText = rulesText,
                featured = featured,
                broadcastAnnouncement = broadcastAnnouncement
            )
            _isBusy.value = false
            res.onSuccess {
                showSuccess("Tournament created!")
                onDone()
            }.onFailure {
                showError(it.unwrapFirestoreMessage("Failed to create tournament"))
            }
        }
    }

    fun updateTournament(
        tournamentId: String,
        updates: Map<String, Any?>,
        successMessage: String = "Tournament updated!",
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.updateTournament(tournamentId, updates)
            _isBusy.value = false
            res.onSuccess {
                showSuccess(successMessage)
                onDone()
            }.onFailure {
                showError(it.unwrapFirestoreMessage("Failed to update tournament"))
            }
        }
    }

    fun deleteTournament(tournamentId: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.deleteTournament(tournamentId)
            _isBusy.value = false
            res.onSuccess { showSuccess("Tournament deleted") }
                .onFailure { showError(it.unwrapFirestoreMessage("Failed to delete tournament")) }
        }
    }

    fun updateRegistrationStatus(
        registration: Registration,
        newStatus: RegistrationStatus,
        adminNote: String = ""
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.updateRegistrationStatus(registration, newStatus, adminNote)
            _isBusy.value = false
            res.onSuccess {
                showSuccess("${registration.ign} marked ${newStatus.label}")
            }.onFailure {
                showError(it.unwrapFirestoreMessage("Failed to update registration"))
            }
        }
    }

    fun saveRoomAccess(
        tournament: Tournament,
        roomId: String,
        roomPassword: String,
        notes: String,
        existingRoom: RoomAccess?,
        announceToAll: Boolean,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.saveRoomAccess(
                tournament = tournament,
                roomId = roomId,
                roomPassword = roomPassword,
                notes = notes,
                existingRoom = existingRoom,
                announceToAll = announceToAll
            )
            _isBusy.value = false
            res.onSuccess {
                showSuccess("Room credentials saved securely!")
                onDone()
            }.onFailure {
                showError(it.unwrapFirestoreMessage("Failed to save room details"))
            }
        }
    }

    fun upsertLeaderboardEntry(
        tournament: Tournament,
        userId: String,
        playerName: String,
        ign: String,
        teamName: String,
        freeFireUid: String,
        kills: Long,
        placementRank: Long,
        positionPoints: Long,
        winnerBadge: WinnerBadge,
        existingEntry: LeaderboardEntry?,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val allPlayers = (publicPlayersState.value as? UiState.Success)?.data ?: emptyList()
            val targetPlayer = allPlayers.firstOrNull {
                (userId.isNotBlank() && it.userId == userId) ||
                    (freeFireUid.isNotBlank() && it.freeFireUid == freeFireUid)
            }
            val res = repository.upsertLeaderboardEntry(
                tournament = tournament,
                userId = userId,
                playerName = playerName,
                ign = ign,
                teamName = teamName,
                freeFireUid = freeFireUid,
                kills = kills,
                placementRank = placementRank,
                positionPoints = positionPoints,
                winnerBadge = winnerBadge,
                existingEntry = existingEntry,
                targetPublicPlayer = targetPlayer
            )
            _isBusy.value = false
            res.onSuccess {
                showSuccess("Leaderboard & player stats updated!")
                onDone()
            }.onFailure {
                showError(it.unwrapFirestoreMessage("Failed to save match result"))
            }
        }
    }

    fun deleteLeaderboardEntry(entryId: String) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.deleteLeaderboardEntry(entryId)
                .onSuccess { showSuccess("Entry removed") }
                .onFailure { showError(it.unwrapFirestoreMessage("Failed to remove entry")) }
            _isBusy.value = false
        }
    }

    fun announceWinners(
        tournament: Tournament,
        winnerName: String,
        runnerUpName: String,
        thirdPlaceName: String,
        mvpName: String,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.announceTournamentWinners(
                tournament = tournament,
                winnerName = winnerName,
                runnerUpName = runnerUpName,
                thirdPlaceName = thirdPlaceName,
                mvpName = mvpName
            ).onSuccess {
                showSuccess("Winners announced!")
                onDone()
            }.onFailure {
                showError(it.unwrapFirestoreMessage("Failed to announce winners"))
            }
            _isBusy.value = false
        }
    }

    fun sendAnnouncement(
        title: String,
        message: String,
        type: AnnouncementType,
        tournamentId: String?,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.sendAnnouncement(title, message, type, tournamentId)
                .onSuccess {
                    showSuccess("Announcement published!")
                    onDone()
                }.onFailure {
                    showError(it.unwrapFirestoreMessage("Failed to publish announcement"))
                }
            _isBusy.value = false
        }
    }

    fun sendDirectNotification(
        targetUserId: String,
        title: String,
        message: String,
        type: NotificationType,
        tournamentId: String?,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.sendDirectNotification(targetUserId, title, message, type, tournamentId)
                .onSuccess {
                    showSuccess("Notification sent to player!")
                    onDone()
                }.onFailure {
                    showError(it.unwrapFirestoreMessage("Failed to send notification"))
                }
            _isBusy.value = false
        }
    }

    fun grantAdminAccess(targetUid: String, targetEmail: String) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.grantAdminAccess(targetUid, targetEmail)
                .onSuccess { showSuccess("Admin access granted") }
                .onFailure { showError(it.unwrapFirestoreMessage("Failed to grant admin")) }
            _isBusy.value = false
        }
    }

    fun revokeAdminAccess(targetUid: String) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.revokeAdminAccess(targetUid)
                .onSuccess { showSuccess("Admin access revoked") }
                .onFailure { showError(it.unwrapFirestoreMessage("Failed to revoke admin")) }
            _isBusy.value = false
        }
    }

    companion object {
        private const val TAG = "ArenaVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun defaultPlacementPoints(rank: Int): Long = when (rank) {
            1 -> 12L
            2 -> 9L
            3 -> 8L
            4 -> 7L
            5 -> 6L
            6 -> 5L
            7 -> 4L
            8 -> 3L
            9 -> 2L
            10 -> 1L
            else -> 0L
        }
    }
}

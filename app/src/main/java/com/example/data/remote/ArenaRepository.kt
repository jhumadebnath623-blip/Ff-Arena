package com.example.data.remote

import android.content.Context
import com.example.R
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
import com.example.data.model.UserNotification
import com.example.data.model.WinnerBadge
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

const val PRIMARY_ADMIN_EMAIL = "jhumadebnath623@gmail.com"

class ArenaRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun <T> Flow<T>.handleSnapshotErrors(op: OperationType, path: String, uid: String): Flow<T> =
        this.catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val exceptionToLog = firestoreError ?: (error as? Exception)
            if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                handleFirestoreError(exceptionToLog, op, path)
            }
            throw error
        }

    // --- REAL-TIME STREAMS ---

    fun observeTournaments(): Flow<List<Tournament>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("tournaments")
                .orderBy("matchTimeMillis", Query.Direction.ASCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Tournament::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, "tournaments", uid)
        )
    }

    fun observePublicPlayers(): Flow<List<PublicPlayer>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("public_players")
                .snapshots()
                .map { snap ->
                    snap.toObjects(PublicPlayer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, "public_players", uid)
        )
    }

    fun observePublicPlayer(userId: String): Flow<PublicPlayer?> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("public_players").document(userId)
                .snapshots()
                .map { snap ->
                    snap.toObject(PublicPlayer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.GET, "public_players/$userId", uid)
        )
    }

    fun observePrivatePlayer(userId: String): Flow<PrivatePlayer?> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("players").document(userId)
                .snapshots()
                .map { snap ->
                    snap.toObject(PrivatePlayer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.GET, "players/$userId", uid)
        )
    }

    fun observeMyRegistrations(userId: String): Flow<List<Registration>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("registrations")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Registration::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, "registrations", uid)
        )
    }

    fun observeAllRegistrations(): Flow<List<Registration>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("registrations")
                .snapshots()
                .map { snap ->
                    snap.toObjects(Registration::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, "registrations", uid)
        )
    }

    fun observeRoomAccess(tournamentId: String): Flow<RoomAccess?> = flow {
        val uid = requireUserId()
        val path = "tournaments/$tournamentId/room_access/credentials"
        emitAll(
            db.collection("tournaments")
                .document(tournamentId)
                .collection("room_access")
                .document("credentials")
                .snapshots()
                .map { snap ->
                    snap.toObject(RoomAccess::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.GET, path, uid)
        )
    }

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("leaderboard")
                .snapshots()
                .map { snap ->
                    snap.toObjects(LeaderboardEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, "leaderboard", uid)
        )
    }

    fun observeAnnouncements(): Flow<List<Announcement>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("announcements")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Announcement::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, "announcements", uid)
        )
    }

    fun observeUserNotifications(userId: String): Flow<List<UserNotification>> = flow {
        val uid = requireUserId()
        val path = "users/$userId/notifications"
        emitAll(
            db.collection("users").document(userId).collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap ->
                    snap.toObjects(UserNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, path, uid)
        )
    }

    fun observeIsAdminDoc(userId: String): Flow<Boolean> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("admins").document(userId)
                .snapshots()
                .map { it.exists() }
                .handleSnapshotErrors(OperationType.GET, "admins/$userId", uid)
        )
    }

    fun observeAdmins(): Flow<List<AdminMember>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection("admins")
                .snapshots()
                .map { snap ->
                    snap.toObjects(AdminMember::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .handleSnapshotErrors(OperationType.LIST, "admins", uid)
        )
    }

    // --- PLAYER PROFILE MUTATIONS ---

    suspend fun savePlayerProfile(
        playerName: String,
        ign: String,
        freeFireUid: String,
        whatsappNumber: String,
        avatarKey: String,
        photoUrl: String?,
        existingPublic: PublicPlayer?,
        existingPrivate: PrivatePlayer?
    ): Result<Unit> {
        var docPath = "public_players"
        return try {
            val uid = requireUserId()
            val email = auth.currentUser?.email?.take(120)?.ifBlank { "player@ffarena.app" } ?: "player@ffarena.app"
            val cleanName = playerName.trim().take(60)
            val cleanIgn = ign.trim().take(40)
            val cleanFfUid = freeFireUid.trim().take(20)
            val cleanWhatsapp = whatsappNumber.trim().take(24)
            val cleanAvatar = avatarKey.trim().ifBlank { "cyber_red" }.take(40)
            val cleanPhoto = photoUrl?.trim()?.take(500)?.takeIf { it.isNotBlank() }

            require(cleanName.length >= 2) { "Player name must be at least 2 characters." }
            require(cleanIgn.length >= 2) { "In-game name (IGN) must be at least 2 characters." }
            require(cleanFfUid.length >= 4) { "Free Fire UID must be at least 4 characters." }
            require(cleanWhatsapp.length >= 6) { "WhatsApp number must be at least 6 characters." }

            val batch = db.batch()
            val publicRef = db.collection("public_players").document(uid)
            val privateRef = db.collection("players").document(uid)
            docPath = publicRef.path

            if (existingPublic == null) {
                val publicPayload = mapOf(
                    "userId" to uid,
                    "playerName" to cleanName,
                    "ign" to cleanIgn,
                    "freeFireUid" to cleanFfUid,
                    "avatarKey" to cleanAvatar,
                    "photoUrl" to cleanPhoto,
                    "totalWins" to 0L,
                    "totalKills" to 0L,
                    "matchesPlayed" to 0L,
                    "totalPoints" to 0L,
                    "achievements" to listOf("Arena Recruit"),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                batch.set(publicRef, publicPayload)
            } else {
                val publicUpdate = mutableMapOf<String, Any>(
                    "playerName" to cleanName,
                    "ign" to cleanIgn,
                    "freeFireUid" to cleanFfUid,
                    "avatarKey" to cleanAvatar,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (cleanPhoto != null) {
                    publicUpdate["photoUrl"] = cleanPhoto
                }
                batch.update(publicRef, publicUpdate)
            }

            if (existingPrivate == null) {
                val privatePayload = mapOf(
                    "userId" to uid,
                    "playerName" to cleanName,
                    "ign" to cleanIgn,
                    "freeFireUid" to cleanFfUid,
                    "whatsappNumber" to cleanWhatsapp,
                    "email" to email,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(privateRef, privatePayload)
            } else {
                batch.update(
                    privateRef,
                    mapOf(
                        "playerName" to cleanName,
                        "ign" to cleanIgn,
                        "freeFireUid" to cleanFfUid,
                        "whatsappNumber" to cleanWhatsapp,
                        "email" to email,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    // --- TOURNAMENT REGISTRATION (ATOMIC SLOT + CONFIRMATION NOTIFICATION) ---

    suspend fun registerForTournament(
        tournament: Tournament,
        playerName: String,
        ign: String,
        freeFireUid: String,
        whatsappNumber: String,
        teamName: String,
        teammateIgns: List<String>,
        teammateUids: List<String>
    ): Result<Unit> {
        val regId = "${tournament.id}_${auth.currentUser?.uid ?: "unauth"}"
        val path = "registrations/$regId"
        return try {
            val uid = requireUserId()
            require(tournament.id.isNotBlank()) { "Invalid tournament ID." }
            require(tournament.registeredSlots < tournament.maxSlots) { "This tournament is already full." }
            val cleanPlayerName = playerName.trim().take(60)
            val cleanIgn = ign.trim().take(40)
            val cleanFfUid = freeFireUid.trim().take(20)
            val cleanWhatsapp = whatsappNumber.trim().take(24)
            val cleanTeamName = teamName.trim().ifBlank { cleanIgn }.take(50)
            val cleanMateIgns = teammateIgns.map { it.trim().take(40) }.filter { it.isNotBlank() }.take(3)
            val cleanMateUids = teammateUids.map { it.trim().take(20) }.filter { it.isNotBlank() }.take(3)

            require(cleanPlayerName.length >= 2) { "Player name is too short." }
            require(cleanIgn.length >= 2) { "IGN is too short." }
            require(cleanFfUid.length >= 4) { "Free Fire UID must be at least 4 characters." }
            require(cleanWhatsapp.length >= 6) { "Enter a valid WhatsApp number." }

            val batch = db.batch()
            val regRef = db.collection("registrations").document("${tournament.id}_$uid")
            val tournamentRef = db.collection("tournaments").document(tournament.id)
            val notifRef = db.collection("users").document(uid)
                .collection("notifications").document("reg_${tournament.id}_${UUID.randomUUID().toString().take(8)}")

            val regPayload = mapOf(
                "userId" to uid,
                "tournamentId" to tournament.id,
                "tournamentTitle" to tournament.title.take(100),
                "mode" to tournament.mode,
                "playerName" to cleanPlayerName,
                "ign" to cleanIgn,
                "freeFireUid" to cleanFfUid,
                "whatsappNumber" to cleanWhatsapp,
                "teamName" to cleanTeamName,
                "teammateIgns" to cleanMateIgns,
                "teammateUids" to cleanMateUids,
                "status" to RegistrationStatus.PENDING.name,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            batch.set(regRef, regPayload)

            batch.update(
                tournamentRef,
                mapOf(
                    "registeredSlots" to (tournament.registeredSlots + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            batch.set(
                notifRef,
                mapOf(
                    "title" to "Registration Submitted",
                    "message" to "Your entry for ${tournament.title} ($cleanTeamName) is pending admin approval.",
                    "type" to NotificationType.REGISTRATION.name,
                    "tournamentId" to tournament.id,
                    "readFlag" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- ADMIN TOURNAMENT MANAGEMENT ---

    suspend fun createTournament(
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
        broadcastAnnouncement: Boolean
    ): Result<String> {
        val docId = "tr_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val path = "tournaments/$docId"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim().take(100)
            require(cleanTitle.length >= 3) { "Tournament title must be at least 3 characters." }
            require(maxSlots in 2L..100L) { "Max slots must be between 2 and 100." }

            val batch = db.batch()
            val ref = db.collection("tournaments").document(docId)
            val payload = mapOf(
                "title" to cleanTitle,
                "mode" to mode,
                "category" to category,
                "status" to status,
                "bannerKey" to bannerKey,
                "mapName" to mapName.trim().ifBlank { "Bermuda" }.take(40),
                "matchTimeMillis" to matchTimeMillis,
                "roomOpenTimeMillis" to roomOpenTimeMillis,
                "maxSlots" to maxSlots,
                "registeredSlots" to 0L,
                "entryType" to "FREE",
                "prizeDetails" to prizeDetails.trim().ifBlank { "Championship Trophy & MVP Title" }.take(240),
                "rulesText" to rulesText.trim().ifBlank { "Fair play only. No emulators or teaming." }.take(3000),
                "featured" to featured,
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            batch.set(ref, payload)

            if (broadcastAnnouncement) {
                val annId = "ann_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
                batch.set(
                    db.collection("announcements").document(annId),
                    mapOf(
                        "title" to "New Tournament: $cleanTitle",
                        "message" to "Free entry $mode ($category) is now open for registration! Slots: $maxSlots.",
                        "type" to AnnouncementType.NEW_TOURNAMENT.name,
                        "tournamentId" to docId,
                        "createdBy" to uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateTournament(
        tournamentId: String,
        updates: Map<String, Any?>
    ): Result<Unit> {
        val path = "tournaments/$tournamentId"
        return try {
            requireUserId()
            require(tournamentId.isNotBlank()) { "Tournament ID required." }
            val safeUpdates = updates.toMutableMap().apply {
                this["updatedAt"] = FieldValue.serverTimestamp()
            }.filterValues { it != null }
            db.collection("tournaments").document(tournamentId).update(safeUpdates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteTournament(tournamentId: String): Result<Unit> {
        val path = "tournaments/$tournamentId"
        return try {
            requireUserId()
            db.collection("tournaments").document(tournamentId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- ADMIN REGISTRATIONS ---

    suspend fun updateRegistrationStatus(
        registration: Registration,
        newStatus: RegistrationStatus,
        adminNote: String
    ): Result<Unit> {
        val path = "registrations/${registration.id}"
        return try {
            requireUserId()
            val batch = db.batch()
            val regRef = db.collection("registrations").document(registration.id)
            val cleanNote = adminNote.trim().take(200).takeIf { it.isNotBlank() }

            val updates = mutableMapOf<String, Any>(
                "status" to newStatus.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (cleanNote != null) {
                updates["adminNote"] = cleanNote
            }
            batch.update(regRef, updates)

            val notifId = "status_${registration.tournamentId}_${UUID.randomUUID().toString().take(8)}"
            val notifRef = db.collection("users")
                .document(registration.userId)
                .collection("notifications")
                .document(notifId)

            val title = when (newStatus) {
                RegistrationStatus.APPROVED -> "Slot Approved: ${registration.tournamentTitle}"
                RegistrationStatus.REJECTED -> "Registration Update: ${registration.tournamentTitle}"
                RegistrationStatus.PENDING -> "Status Reset: ${registration.tournamentTitle}"
            }
            val msg = when (newStatus) {
                RegistrationStatus.APPROVED -> "Your squad (${registration.teamName}) is confirmed! Room ID & Password will unlock in Tournament Details."
                RegistrationStatus.REJECTED -> "Your registration was not approved.${if (cleanNote != null) " Note: $cleanNote" else ""}"
                RegistrationStatus.PENDING -> "Your registration is back under review."
            }
            batch.set(
                notifRef,
                mapOf(
                    "title" to title.take(100),
                    "message" to msg.take(600),
                    "type" to NotificationType.APPROVAL.name,
                    "tournamentId" to registration.tournamentId,
                    "readFlag" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- ADMIN ROOM DETAILS ---

    suspend fun saveRoomAccess(
        tournament: Tournament,
        roomId: String,
        roomPassword: String,
        notes: String,
        existingRoom: RoomAccess?,
        announceToAll: Boolean
    ): Result<Unit> {
        val path = "tournaments/${tournament.id}/room_access/credentials"
        return try {
            val uid = requireUserId()
            val cleanRoomId = roomId.trim().take(40)
            val cleanPass = roomPassword.trim().take(40)
            val cleanNotes = notes.trim().take(300)
            require(cleanRoomId.length >= 2) { "Room ID must be at least 2 characters." }
            require(cleanPass.isNotEmpty()) { "Room Password cannot be empty." }

            val batch = db.batch()
            val roomRef = db.collection("tournaments")
                .document(tournament.id)
                .collection("room_access")
                .document("credentials")

            if (existingRoom == null) {
                batch.set(
                    roomRef,
                    mapOf(
                        "tournamentId" to tournament.id,
                        "roomId" to cleanRoomId,
                        "roomPassword" to cleanPass,
                        "notes" to cleanNotes,
                        "updatedBy" to uid,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.update(
                    roomRef,
                    mapOf(
                        "roomId" to cleanRoomId,
                        "roomPassword" to cleanPass,
                        "notes" to cleanNotes,
                        "updatedBy" to uid,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            if (announceToAll) {
                val annId = "room_${tournament.id}_${UUID.randomUUID().toString().take(6)}"
                batch.set(
                    db.collection("announcements").document(annId),
                    mapOf(
                        "title" to "Room Details Ready: ${tournament.title}",
                        "message" to "Approved players can now view the Custom Room ID & Password inside Tournament Details. Be in lobby 10 mins early!",
                        "type" to AnnouncementType.ROOM_READY.name,
                        "tournamentId" to tournament.id,
                        "createdBy" to uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- ADMIN LEADERBOARD & RESULTS ---

    suspend fun upsertLeaderboardEntry(
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
        targetPublicPlayer: PublicPlayer?
    ): Result<Unit> {
        val entryId = existingEntry?.id?.ifBlank { null }
            ?: "${tournament.id}_${userId.ifBlank { UUID.randomUUID().toString().take(8) }}"
        val path = "leaderboard/$entryId"
        return try {
            requireUserId()
            val cleanKills = kills.coerceIn(0L, 1000L)
            val cleanRank = placementRank.coerceIn(1L, 100L)
            val cleanPosPts = positionPoints.coerceIn(0L, 10000L)
            val totalPts = cleanKills + cleanPosPts

            val batch = db.batch()
            val entryRef = db.collection("leaderboard").document(entryId)

            if (existingEntry == null) {
                batch.set(
                    entryRef,
                    mapOf(
                        "tournamentId" to tournament.id,
                        "tournamentTitle" to tournament.title.take(100),
                        "userId" to userId.ifBlank { "external_${entryId}" },
                        "playerName" to playerName.trim().take(60),
                        "ign" to ign.trim().take(40),
                        "teamName" to teamName.trim().ifBlank { ign.trim() }.take(50),
                        "freeFireUid" to freeFireUid.trim().take(20),
                        "kills" to cleanKills,
                        "placementRank" to cleanRank,
                        "positionPoints" to cleanPosPts,
                        "totalPoints" to totalPts,
                        "winnerBadge" to winnerBadge.name,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.update(
                    entryRef,
                    mapOf(
                        "tournamentTitle" to tournament.title.take(100),
                        "playerName" to playerName.trim().take(60),
                        "ign" to ign.trim().take(40),
                        "teamName" to teamName.trim().ifBlank { ign.trim() }.take(50),
                        "freeFireUid" to freeFireUid.trim().take(20),
                        "kills" to cleanKills,
                        "placementRank" to cleanRank,
                        "positionPoints" to cleanPosPts,
                        "totalPoints" to totalPts,
                        "winnerBadge" to winnerBadge.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            // Sync career totals to public_players if linked player profile exists
            if (targetPublicPlayer != null) {
                val deltaKills = cleanKills - (existingEntry?.kills ?: 0L)
                val deltaPts = totalPts - (existingEntry?.totalPoints ?: 0L)
                val wasWin = existingEntry?.winnerBadge == WinnerBadge.CHAMPION.name
                val isWin = winnerBadge == WinnerBadge.CHAMPION
                val deltaWins = when {
                    !wasWin && isWin -> 1L
                    wasWin && !isWin -> -1L
                    else -> 0L
                }
                val deltaMatches = if (existingEntry == null) 1L else 0L

                val newKills = (targetPublicPlayer.totalKills + deltaKills).coerceAtLeast(0L)
                val newWins = (targetPublicPlayer.totalWins + deltaWins).coerceAtLeast(0L)
                val newMatches = (targetPublicPlayer.matchesPlayed + deltaMatches).coerceAtLeast(0L)
                val newTotalPts = (targetPublicPlayer.totalPoints + deltaPts).coerceAtLeast(0L)

                val achievements = targetPublicPlayer.achievements.toMutableSet()
                if (newMatches >= 1) achievements.add("First Drop")
                if (newKills >= 10) achievements.add("Sharpshooter (10+ Kills)")
                if (newKills >= 50) achievements.add("Terminator (50+ Kills)")
                if (newWins >= 1) achievements.add("Booyah Champion")
                if (newWins >= 3) achievements.add("Arena Legend")

                batch.update(
                    db.collection("public_players").document(targetPublicPlayer.userId),
                    mapOf(
                        "totalKills" to newKills,
                        "totalWins" to newWins,
                        "matchesPlayed" to newMatches,
                        "totalPoints" to newTotalPts,
                        "achievements" to achievements.take(20).toList(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            if (targetPublicPlayer != null && existingEntry == null) {
                val notifRef = db.collection("users")
                    .document(targetPublicPlayer.userId)
                    .collection("notifications")
                    .document("res_${tournament.id}_${UUID.randomUUID().toString().take(6)}")
                batch.set(
                    notifRef,
                    mapOf(
                        "title" to "Match Results Posted: ${tournament.title}",
                        "message" to "Rank #$cleanRank • $cleanKills Kills • $totalPts Total Points.",
                        "type" to if (winnerBadge == WinnerBadge.CHAMPION) NotificationType.WINNER.name else NotificationType.RESULT.name,
                        "tournamentId" to tournament.id,
                        "readFlag" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteLeaderboardEntry(entryId: String): Result<Unit> {
        val path = "leaderboard/$entryId"
        return try {
            requireUserId()
            db.collection("leaderboard").document(entryId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun announceTournamentWinners(
        tournament: Tournament,
        winnerName: String,
        runnerUpName: String,
        thirdPlaceName: String,
        mvpName: String
    ): Result<Unit> {
        val path = "tournaments/${tournament.id}"
        return try {
            val uid = requireUserId()
            val cleanWinner = winnerName.trim().take(80)
            require(cleanWinner.isNotEmpty()) { "Winner name is required." }

            val batch = db.batch()
            val tRef = db.collection("tournaments").document(tournament.id)
            val updates = mutableMapOf<String, Any>(
                "status" to "COMPLETED",
                "winnerName" to cleanWinner,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            runnerUpName.trim().take(80).takeIf { it.isNotEmpty() }?.let { updates["runnerUpName"] = it }
            thirdPlaceName.trim().take(80).takeIf { it.isNotEmpty() }?.let { updates["thirdPlaceName"] = it }
            mvpName.trim().take(80).takeIf { it.isNotEmpty() }?.let { updates["mvpName"] = it }
            batch.update(tRef, updates)

            val annId = "win_${tournament.id}_${UUID.randomUUID().toString().take(6)}"
            val mvpSnippet = mvpName.trim().takeIf { it.isNotEmpty() }?.let { " | MVP: $it" } ?: ""
            batch.set(
                db.collection("announcements").document(annId),
                mapOf(
                    "title" to "Champion Crowned: ${tournament.title}",
                    "message" to "1st Place: $cleanWinner$mvpSnippet. Full standings are live on the Leaderboard!",
                    "type" to AnnouncementType.WINNER.name,
                    "tournamentId" to tournament.id,
                    "createdBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- NOTIFICATIONS & ANNOUNCEMENTS ---

    suspend fun sendAnnouncement(
        title: String,
        message: String,
        type: AnnouncementType,
        tournamentId: String?
    ): Result<Unit> {
        val id = "ann_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val path = "announcements/$id"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim().take(100)
            val cleanMessage = message.trim().take(600)
            require(cleanTitle.length >= 2) { "Title must be at least 2 characters." }
            require(cleanMessage.length >= 2) { "Message must be at least 2 characters." }

            val payload = mapOf(
                "title" to cleanTitle,
                "message" to cleanMessage,
                "type" to type.name,
                "tournamentId" to tournamentId?.takeIf { it.isNotBlank() },
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            db.collection("announcements").document(id).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun sendDirectNotification(
        targetUserId: String,
        title: String,
        message: String,
        type: NotificationType,
        tournamentId: String?
    ): Result<Unit> {
        val id = "notif_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val path = "users/$targetUserId/notifications/$id"
        return try {
            requireUserId()
            val payload = mapOf(
                "title" to title.trim().take(100),
                "message" to message.trim().take(600),
                "type" to type.name,
                "tournamentId" to tournamentId?.takeIf { it.isNotBlank() },
                "readFlag" to false,
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            db.collection("users").document(targetUserId).collection("notifications").document(id).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(userId: String, notificationId: String): Result<Unit> {
        val path = "users/$userId/notifications/$notificationId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cannot modify another user's notification." }
            db.collection("users").document(userId).collection("notifications").document(notificationId)
                .update(mapOf("readFlag" to true)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsRead(userId: String, unreadIds: List<String>): Result<Unit> {
        val path = "users/$userId/notifications"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Cannot modify another user's notifications." }
            if (unreadIds.isEmpty()) return Result.success(Unit)
            val batch = db.batch()
            unreadIds.take(50).forEach { id ->
                val ref = db.collection("users").document(userId).collection("notifications").document(id)
                batch.update(ref, mapOf("readFlag" to true))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- ADMIN ACCESS MANAGEMENT ---

    suspend fun grantAdminAccess(targetUid: String, targetEmail: String): Result<Unit> {
        val cleanUid = targetUid.trim()
        val path = "admins/$cleanUid"
        return try {
            val uid = requireUserId()
            require(cleanUid.matches(Regex("^[a-zA-Z0-9_\\-]+$"))) { "Invalid UID format." }
            db.collection("admins").document(cleanUid).set(
                mapOf(
                    "uid" to cleanUid,
                    "email" to targetEmail.trim().take(120),
                    "grantedBy" to uid,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun revokeAdminAccess(targetUid: String): Result<Unit> {
        val path = "admins/$targetUid"
        return try {
            requireUserId()
            db.collection("admins").document(targetUid).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}

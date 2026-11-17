package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.model.AdminMember
import com.example.data.model.AnnouncementType
import com.example.data.model.LeaderboardEntry
import com.example.data.model.NotificationType
import com.example.data.model.Registration
import com.example.data.model.RegistrationStatus
import com.example.data.model.RoomAccess
import com.example.data.model.Tournament
import com.example.data.model.WinnerBadge
import com.example.data.remote.onGoogleSignInClicked
import com.example.ui.components.ArenaTextField
import com.example.ui.components.EsportsCard
import com.example.ui.components.RegistrationStatusPill
import com.example.ui.components.StatusPill
import com.example.ui.components.TournamentStatusPill
import com.example.ui.components.formatMatchDateTime
import com.example.ui.state.UiState
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.ArenaCharcoal
import com.example.ui.theme.ArenaOutline
import com.example.ui.theme.ArenaSurface
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LiveEmerald
import com.example.ui.theme.MoltenOrange
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    isAdmin: Boolean,
    currentUserId: String,
    currentUserEmail: String,
    tournamentsState: UiState<List<Tournament>>,
    allRegistrations: List<Registration>,
    leaderboardState: UiState<List<LeaderboardEntry>>,
    adminsList: List<AdminMember>,
    selectedRoomAccess: RoomAccess?,
    isBusy: Boolean,
    onSelectTournamentForRoom: (String?) -> Unit,
    onCreateTournament: (String, String, String, String, String, String, Long, Long, Long, String, String, Boolean, Boolean, () -> Unit) -> Unit,
    onUpdateTournament: (String, Map<String, Any?>, String, () -> Unit) -> Unit,
    onDeleteTournament: (String) -> Unit,
    onUpdateRegistrationStatus: (Registration, RegistrationStatus, String) -> Unit,
    onSaveRoomAccess: (Tournament, String, String, String, RoomAccess?, Boolean, () -> Unit) -> Unit,
    onUpsertLeaderboard: (Tournament, String, String, String, String, String, Long, Long, Long, WinnerBadge, LeaderboardEntry?, () -> Unit) -> Unit,
    onDeleteLeaderboardEntry: (String) -> Unit,
    onAnnounceWinners: (Tournament, String, String, String, String, () -> Unit) -> Unit,
    onSendAnnouncement: (String, String, AnnouncementType, String?, () -> Unit) -> Unit,
    onSendDirectNotification: (String, String, String, NotificationType, String?, () -> Unit) -> Unit,
    onGrantAdmin: (String, String) -> Unit,
    onRevokeAdmin: (String) -> Unit,
    onToastMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    Scaffold(
        containerColor = ArenaBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ArenaCharcoal, titleContentColor = TextPrimary),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = NeonCrimson)
                        Text("ADMIN COMMAND CENTER", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!isAdmin) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EsportsCard(highlightColor = NeonCrimson.copy(alpha = 0.6f), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = NeonCrimson, modifier = Modifier.size(48.dp))
                        Text("RESTRICTED ADMIN PORTAL", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                        Text(
                            text = "Signed in as $currentUserEmail. Only authorized FF Arena admins can manage tournaments, approve players, and set room credentials.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Surface(
                            color = ArenaCharcoal,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ArenaOutline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("YOUR USER UID", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        currentUserId,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = MoltenOrange
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cb.setPrimaryClip(ClipData.newPlainText("UID", currentUserId))
                                        onToastMessage("UID copied!")
                                    }
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy UID", tint = TextPrimary)
                                }
                            }
                        }
                        Button(
                            onClick = {
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = { onToastMessage("Account refreshed") },
                                    onAuthError = { onToastMessage(it) },
                                    scope = scope
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign in with Admin Google Account")
                        }
                    }
                }
            }
            return@Scaffold
        }

        val tournaments = (tournamentsState as? UiState.Success)?.data ?: emptyList()
        val leaderboard = (leaderboardState as? UiState.Success)?.data ?: emptyList()

        var selectedTab by remember { mutableIntStateOf(0) }
        val tabs = listOf("Tournaments", "Registrations", "Rooms & Alerts", "Results & Admins")

        var showCreateEditor by remember { mutableStateOf(false) }
        var editingTournament by remember { mutableStateOf<Tournament?>(null) }
        var winnerTournament by remember { mutableStateOf<Tournament?>(null) }
        var resultTournament by remember { mutableStateOf<Tournament?>(null) }
        var editingLeaderboardEntry by remember { mutableStateOf<LeaderboardEntry?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = ArenaCharcoal,
                contentColor = NeonCrimson,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonCrimson
                    )
                }
            ) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedTab == idx) NeonCrimson else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("admin_tab_$idx")
                    )
                }
            }

            when (selectedTab) {
                0 -> AdminTournamentsTab(
                    tournaments = tournaments,
                    onCreateNew = {
                        editingTournament = null
                        showCreateEditor = true
                    },
                    onEdit = {
                        editingTournament = it
                        showCreateEditor = true
                    },
                    onToggleFeatured = { t ->
                        onUpdateTournament(t.id, mapOf("featured" to !t.featured), "Banner featured status updated") {}
                    },
                    onAnnounceWinners = { winnerTournament = it },
                    onDelete = onDeleteTournament
                )

                1 -> AdminRegistrationsTab(
                    registrations = allRegistrations,
                    onUpdateStatus = onUpdateRegistrationStatus
                )

                2 -> AdminRoomsAndAlertsTab(
                    tournaments = tournaments,
                    registrations = allRegistrations,
                    selectedRoomAccess = selectedRoomAccess,
                    isBusy = isBusy,
                    onSelectTournamentForRoom = onSelectTournamentForRoom,
                    onSaveRoomAccess = onSaveRoomAccess,
                    onSendAnnouncement = onSendAnnouncement,
                    onSendDirectNotification = onSendDirectNotification
                )

                3 -> AdminResultsAndAdminsTab(
                    tournaments = tournaments,
                    leaderboard = leaderboard,
                    adminsList = adminsList,
                    isBusy = isBusy,
                    onAddResultForTournament = {
                        editingLeaderboardEntry = null
                        resultTournament = it
                    },
                    onEditEntry = { entry ->
                        val t = tournaments.firstOrNull { it.id == entry.tournamentId }
                        if (t != null) {
                            editingLeaderboardEntry = entry
                            resultTournament = t
                        }
                    },
                    onDeleteEntry = onDeleteLeaderboardEntry,
                    onGrantAdmin = onGrantAdmin,
                    onRevokeAdmin = onRevokeAdmin
                )
            }
        }

        if (showCreateEditor) {
            TournamentEditorSheet(
                existing = editingTournament,
                isBusy = isBusy,
                onDismiss = { showCreateEditor = false },
                onSave = { title, mode, cat, status, banner, map, matchMillis, roomMillis, maxSlots, prize, rules, featured, broadcast ->
                    val current = editingTournament
                    if (current == null) {
                        onCreateTournament(title, mode, cat, status, banner, map, matchMillis, roomMillis, maxSlots, prize, rules, featured, broadcast) {
                            showCreateEditor = false
                        }
                    } else {
                        onUpdateTournament(
                            current.id,
                            mapOf(
                                "title" to title,
                                "mode" to mode,
                                "category" to cat,
                                "status" to status,
                                "bannerKey" to banner,
                                "mapName" to map,
                                "matchTimeMillis" to matchMillis,
                                "roomOpenTimeMillis" to roomMillis,
                                "maxSlots" to maxSlots,
                                "prizeDetails" to prize,
                                "rulesText" to rules,
                                "featured" to featured
                            ),
                            "Tournament updated!"
                        ) {
                            showCreateEditor = false
                        }
                    }
                }
            )
        }

        winnerTournament?.let { t ->
            WinnerAnnouncementSheet(
                tournament = t,
                isBusy = isBusy,
                onDismiss = { winnerTournament = null },
                onPublish = { w, r, third, mvp ->
                    onAnnounceWinners(t, w, r, third, mvp) {
                        winnerTournament = null
                    }
                }
            )
        }

        resultTournament?.let { t ->
            val approvedForT = allRegistrations.filter {
                it.tournamentId == t.id && it.statusEnum == RegistrationStatus.APPROVED
            }
            LeaderboardResultSheet(
                tournament = t,
                approvedRegs = approvedForT,
                existingEntry = editingLeaderboardEntry,
                isBusy = isBusy,
                onDismiss = { resultTournament = null },
                onSave = { uid, pName, ign, team, ffUid, kills, rank, posPts, badge ->
                    onUpsertLeaderboard(t, uid, pName, ign, team, ffUid, kills, rank, posPts, badge, editingLeaderboardEntry) {
                        resultTournament = null
                    }
                }
            )
        }
    }
}

@Composable
private fun AdminTournamentsTab(
    tournaments: List<Tournament>,
    onCreateNew: () -> Unit,
    onEdit: (Tournament) -> Unit,
    onToggleFeatured: (Tournament) -> Unit,
    onAnnounceWinners: (Tournament) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onCreateNew,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admin_create_tournament_btn")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create New Tournament", style = MaterialTheme.typography.labelLarge)
            }
        }
        items(tournaments, key = { it.id }) { t ->
            EsportsCard(highlightColor = if (t.featured) MoltenOrange.copy(alpha = 0.6f) else ArenaOutline) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                        TournamentStatusPill(t.statusEnum)
                    }
                    Text(
                        "${t.modeEnum.label} • ${t.categoryEnum.label} • ${t.registeredSlots}/${t.maxSlots} Slots • Banner: ${t.bannerEnum.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(formatMatchDateTime(t.matchTimeMillis), style = MaterialTheme.typography.labelSmall, color = MoltenOrange)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { onEdit(t) }, border = BorderStroke(1.dp, ArenaOutline)) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit / Banner")
                        }
                        OutlinedButton(onClick = { onToggleFeatured(t) }, border = BorderStroke(1.dp, MoltenOrange)) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = MoltenOrange, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (t.featured) "Unfeature" else "Feature", color = MoltenOrange)
                        }
                        OutlinedButton(onClick = { onAnnounceWinners(t) }, border = BorderStroke(1.dp, ChampionshipGold)) {
                            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Winners", color = ChampionshipGold)
                        }
                        OutlinedButton(onClick = { onDelete(t.id) }, border = BorderStroke(1.dp, ErrorRed)) {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRegistrationsTab(
    registrations: List<Registration>,
    onUpdateStatus: (Registration, RegistrationStatus, String) -> Unit
) {
    var statusFilter by remember { mutableStateOf<RegistrationStatus?>(RegistrationStatus.PENDING) }
    val filtered = remember(registrations, statusFilter) {
        if (statusFilter == null) registrations else registrations.filter { it.statusEnum == statusFilter }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { statusFilter = null },
                    label = { Text("All (${registrations.size})") },
                    colors = adminChipColors()
                )
                RegistrationStatus.entries.forEach { st ->
                    val count = registrations.count { it.statusEnum == st }
                    FilterChip(
                        selected = statusFilter == st,
                        onClick = { statusFilter = st },
                        label = { Text("${st.label} ($count)") },
                        colors = adminChipColors()
                    )
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EsportsCard {
                    Text(
                        "No registrations match the current filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(filtered, key = { it.id }) { reg ->
                EsportsCard {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(reg.tournamentTitle, style = MaterialTheme.typography.labelMedium, color = MoltenOrange, modifier = Modifier.weight(1f))
                            RegistrationStatusPill(reg.statusEnum)
                        }
                        Text("${reg.teamName} • ${reg.ign}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            "Captain: ${reg.playerName} • UID: ${reg.freeFireUid} • WA: ${reg.whatsappNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        if (reg.teammateIgns.isNotEmpty()) {
                            Text(
                                "Roster: ${reg.teammateIgns.zip(reg.teammateUids).joinToString { "${it.first} (${it.second})" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onUpdateStatus(reg, RegistrationStatus.APPROVED, "Approved by Admin") },
                                colors = ButtonDefaults.buttonColors(containerColor = LiveEmerald, contentColor = ArenaBlack),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Approve")
                            }
                            OutlinedButton(
                                onClick = { onUpdateStatus(reg, RegistrationStatus.REJECTED, "Invalid details or full slot") },
                                border = BorderStroke(1.dp, ErrorRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reject", color = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRoomsAndAlertsTab(
    tournaments: List<Tournament>,
    registrations: List<Registration>,
    selectedRoomAccess: RoomAccess?,
    isBusy: Boolean,
    onSelectTournamentForRoom: (String?) -> Unit,
    onSaveRoomAccess: (Tournament, String, String, String, RoomAccess?, Boolean, () -> Unit) -> Unit,
    onSendAnnouncement: (String, String, AnnouncementType, String?, () -> Unit) -> Unit,
    onSendDirectNotification: (String, String, String, NotificationType, String?, () -> Unit) -> Unit
) {
    var selectedTournament by remember(tournaments) { mutableStateOf(tournaments.firstOrNull()) }
    LaunchedEffect(selectedTournament?.id) {
        onSelectTournamentForRoom(selectedTournament?.id)
    }

    var roomId by remember(selectedRoomAccess, selectedTournament?.id) { mutableStateOf(selectedRoomAccess?.roomId ?: "") }
    var roomPass by remember(selectedRoomAccess, selectedTournament?.id) { mutableStateOf(selectedRoomAccess?.roomPassword ?: "") }
    var roomNotes by remember(selectedRoomAccess, selectedTournament?.id) { mutableStateOf(selectedRoomAccess?.notes ?: "") }
    var announceRoom by remember { mutableStateOf(true) }

    var annTitle by remember { mutableStateOf("") }
    var annBody by remember { mutableStateOf("") }
    var annType by remember { mutableStateOf(AnnouncementType.MATCH_REMINDER) }

    var targetUid by remember { mutableStateOf("") }
    var directTitle by remember { mutableStateOf("") }
    var directBody by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Room Credentials Section
        item {
            EsportsCard(highlightColor = MoltenOrange.copy(alpha = 0.5f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("SET CUSTOM ROOM ID & PASSWORD", style = MaterialTheme.typography.titleMedium, color = MoltenOrange)
                    if (tournaments.isEmpty()) {
                        Text("Create a tournament first to configure room details.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    } else {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tournaments.forEach { t ->
                                FilterChip(
                                    selected = selectedTournament?.id == t.id,
                                    onClick = { selectedTournament = t },
                                    label = { Text(t.title) },
                                    colors = adminChipColors()
                                )
                            }
                        }
                        ArenaTextField(roomId, { roomId = it }, "Custom Room ID *", modifier = Modifier.testTag("admin_room_id_input"))
                        ArenaTextField(roomPass, { roomPass = it }, "Room Password *", modifier = Modifier.testTag("admin_room_pass_input"))
                        ArenaTextField(roomNotes, { roomNotes = it }, "Lobby Notes (e.g. Slot # list)")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Broadcast 'Room Ready' Alert", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Switch(
                                checked = announceRoom,
                                onCheckedChange = { announceRoom = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = LiveEmerald)
                            )
                        }
                        Button(
                            onClick = {
                                selectedTournament?.let { t ->
                                    onSaveRoomAccess(t, roomId, roomPass, roomNotes, selectedRoomAccess, announceRoom) {}
                                }
                            },
                            enabled = !isBusy && selectedTournament != null && roomId.trim().length >= 2 && roomPass.trim().isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = LiveEmerald, contentColor = ArenaBlack),
                            modifier = Modifier.fillMaxWidth().testTag("admin_save_room_btn")
                        ) {
                            Text("Save Room Credentials", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Global Announcement Broadcast
        item {
            EsportsCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = NeonCrimson)
                        Text("SEND GLOBAL ANNOUNCEMENT", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnnouncementType.entries.forEach { type ->
                            FilterChip(
                                selected = annType == type,
                                onClick = { annType = type },
                                label = { Text(type.label) },
                                colors = adminChipColors()
                            )
                        }
                    }
                    ArenaTextField(annTitle, { annTitle = it }, "Announcement Title *")
                    ArenaTextField(annBody, { annBody = it }, "Message *", singleLine = false, minLines = 2)
                    Button(
                        onClick = {
                            onSendAnnouncement(annTitle, annBody, annType, selectedTournament?.id) {
                                annTitle = ""
                                annBody = ""
                            }
                        },
                        enabled = !isBusy && annTitle.trim().length >= 2 && annBody.trim().length >= 2,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Publish Announcement", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Direct Player Notification
        item {
            EsportsCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("DIRECT PLAYER NOTIFICATION", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    if (registrations.isNotEmpty()) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            registrations.distinctBy { it.userId }.forEach { reg ->
                                FilterChip(
                                    selected = targetUid == reg.userId,
                                    onClick = { targetUid = reg.userId },
                                    label = { Text(reg.ign) },
                                    colors = adminChipColors(MoltenOrange)
                                )
                            }
                        }
                    }
                    ArenaTextField(targetUid, { targetUid = it }, "Target User UID *")
                    ArenaTextField(directTitle, { directTitle = it }, "Notification Title *")
                    ArenaTextField(directBody, { directBody = it }, "Notification Message *")
                    Button(
                        onClick = {
                            onSendDirectNotification(targetUid, directTitle, directBody, NotificationType.REMINDER, selectedTournament?.id) {
                                directTitle = ""
                                directBody = ""
                            }
                        },
                        enabled = !isBusy && targetUid.isNotBlank() && directTitle.trim().length >= 2 && directBody.trim().length >= 2,
                        colors = ButtonDefaults.buttonColors(containerColor = MoltenOrange, contentColor = ArenaBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Player Alert", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminResultsAndAdminsTab(
    tournaments: List<Tournament>,
    leaderboard: List<LeaderboardEntry>,
    adminsList: List<AdminMember>,
    isBusy: Boolean,
    onAddResultForTournament: (Tournament) -> Unit,
    onEditEntry: (LeaderboardEntry) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onGrantAdmin: (String, String) -> Unit,
    onRevokeAdmin: (String) -> Unit
) {
    var selectedTournament by remember(tournaments) { mutableStateOf(tournaments.firstOrNull()) }
    var newAdminUid by remember { mutableStateOf("") }
    var newAdminEmail by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            EsportsCard(highlightColor = ChampionshipGold.copy(alpha = 0.5f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("MATCH RESULTS & LEADERBOARD MANAGER", style = MaterialTheme.typography.titleMedium, color = ChampionshipGold)
                    if (tournaments.isEmpty()) {
                        Text("Create a tournament first to score match results.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    } else {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tournaments.forEach { t ->
                                FilterChip(
                                    selected = selectedTournament?.id == t.id,
                                    onClick = { selectedTournament = t },
                                    label = { Text(t.title) },
                                    colors = adminChipColors(ChampionshipGold)
                                )
                            }
                        }
                        Button(
                            onClick = { selectedTournament?.let(onAddResultForTournament) },
                            enabled = selectedTournament != null,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Add Kills & Placement Score", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        val filteredEntries = leaderboard.filter { selectedTournament == null || it.tournamentId == selectedTournament?.id }
            .sortedByDescending { it.totalPoints }

        items(filteredEntries, key = { it.id }) { entry ->
            EsportsCard {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("#${entry.placementRank} • ${entry.teamName} (${entry.ign})", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text(
                            "Kills: ${entry.kills} • Pos: ${entry.positionPoints} • Total: ${entry.totalPoints} pts",
                            style = MaterialTheme.typography.bodySmall,
                            color = LiveEmerald
                        )
                    }
                    IconButton(onClick = { onEditEntry(entry) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Score", tint = MoltenOrange)
                    }
                    IconButton(onClick = { onDeleteEntry(entry.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Score", tint = ErrorRed)
                    }
                }
            }
        }

        // Admin Access Manager
        item {
            EsportsCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("MANAGE CO-ADMINS", style = MaterialTheme.typography.titleMedium, color = NeonCrimson)
                    ArenaTextField(newAdminUid, { newAdminUid = it }, "User Firebase UID *")
                    ArenaTextField(newAdminEmail, { newAdminEmail = it }, "User Google Email *")
                    Button(
                        onClick = {
                            onGrantAdmin(newAdminUid, newAdminEmail)
                            newAdminUid = ""
                            newAdminEmail = ""
                        },
                        enabled = !isBusy && newAdminUid.isNotBlank() && newAdminEmail.contains("@"),
                        colors = ButtonDefaults.buttonColors(containerColor = MoltenOrange, contentColor = ArenaBlack),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Admin Access", style = MaterialTheme.typography.labelLarge)
                    }
                    adminsList.forEach { admin ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(admin.email, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text(admin.uid, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            IconButton(onClick = { onRevokeAdmin(admin.uid) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Revoke Admin", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

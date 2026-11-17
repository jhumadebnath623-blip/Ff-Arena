package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.BannerKey
import com.example.data.model.LeaderboardEntry
import com.example.data.model.Registration
import com.example.data.model.RoomAccess
import com.example.data.model.Tournament
import com.example.data.model.TournamentCategory
import com.example.data.model.TournamentMode
import com.example.data.model.TournamentStatus
import com.example.data.model.WinnerBadge
import com.example.ui.components.ArenaTextField
import com.example.ui.components.formatMatchDateTime
import com.example.ui.state.ArenaViewModel
import com.example.ui.theme.ArenaCharcoal
import com.example.ui.theme.ArenaOutline
import com.example.ui.theme.ArenaSurface
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LiveEmerald
import com.example.ui.theme.MoltenOrange
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentEditorSheet(
    existing: Tournament?,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var mode by remember { mutableStateOf(existing?.modeEnum ?: TournamentMode.SQUAD) }
    var category by remember { mutableStateOf(existing?.categoryEnum ?: TournamentCategory.BATTLE_ROYALE) }
    var status by remember { mutableStateOf(existing?.statusEnum ?: TournamentStatus.UPCOMING) }
    var bannerKey by remember { mutableStateOf(existing?.bannerEnum ?: BannerKey.BATTLE_ROYALE) }
    var mapName by remember { mutableStateOf(existing?.mapName ?: "Bermuda") }
    var matchTimeMillis by remember {
        mutableLongStateOf(existing?.matchTimeMillis ?: (System.currentTimeMillis() + 3_600_000L))
    }
    var maxSlotsText by remember { mutableStateOf((existing?.maxSlots ?: 12L).toString()) }
    var prizeDetails by remember { mutableStateOf(existing?.prizeDetails ?: "Champion Crown + MVP Title + Hall of Fame") }
    var rulesText by remember {
        mutableStateOf(
            existing?.rulesText
                ?: "1. Mobile devices only (no emulators).\n2. Exact registered IGN & UID required.\n3. No teaming or third-party tools.\n4. Room opens 15 mins before match start."
        )
    }
    var featured by remember { mutableStateOf(existing?.featured ?: true) }
    var broadcast by remember { mutableStateOf(existing == null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ArenaSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (existing == null) "CREATE TOURNAMENT" else "EDIT TOURNAMENT",
                style = MaterialTheme.typography.headlineSmall,
                color = NeonCrimson
            )

            ArenaTextField(title, { title = it }, "Tournament Title *", modifier = Modifier.testTag("admin_t_title"))

            Text("MODE", style = MaterialTheme.typography.labelMedium, color = MoltenOrange)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TournamentMode.entries.forEach { m ->
                    FilterChip(
                        selected = mode == m,
                        onClick = {
                            mode = m
                            if (existing == null) {
                                maxSlotsText = when (m) {
                                    TournamentMode.SOLO -> "48"
                                    TournamentMode.DUO -> "24"
                                    TournamentMode.SQUAD -> "12"
                                }
                            }
                        },
                        label = { Text(m.label) },
                        colors = adminChipColors()
                    )
                }
            }

            Text("CATEGORY", style = MaterialTheme.typography.labelMedium, color = MoltenOrange)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TournamentCategory.entries.forEach { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = {
                            category = c
                            if (existing == null) {
                                bannerKey = if (c == TournamentCategory.CLASH_SQUAD) BannerKey.CLASH_SQUAD else BannerKey.BATTLE_ROYALE
                            }
                        },
                        label = { Text(c.label) },
                        colors = adminChipColors()
                    )
                }
            }

            Text("STATUS", style = MaterialTheme.typography.labelMedium, color = MoltenOrange)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TournamentStatus.entries.forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text(s.label) },
                        colors = adminChipColors()
                    )
                }
            }

            Text("BANNER ARTWORK", style = MaterialTheme.typography.labelMedium, color = MoltenOrange)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BannerKey.entries.forEach { b ->
                    FilterChip(
                        selected = bannerKey == b,
                        onClick = { bannerKey = b },
                        label = { Text(b.label) },
                        colors = adminChipColors(MoltenOrange)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArenaTextField(mapName, { mapName = it }, "Map Name *", modifier = Modifier.weight(1f))
                ArenaTextField(
                    maxSlotsText,
                    { maxSlotsText = it },
                    "Max Slots (2-100) *",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            // Schedule Picker
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("MATCH SCHEDULE: ${formatMatchDateTime(matchTimeMillis)}", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = matchTimeMillis }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    cal.set(y, m, d)
                                    TimePickerDialog(
                                        context,
                                        { _, hr, min ->
                                            cal.set(Calendar.HOUR_OF_DAY, hr)
                                            cal.set(Calendar.MINUTE, min)
                                            matchTimeMillis = cal.timeInMillis
                                        },
                                        cal.get(Calendar.HOUR_OF_DAY),
                                        cal.get(Calendar.MINUTE),
                                        false
                                    ).show()
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        border = BorderStroke(1.dp, ArenaOutline)
                    ) {
                        Text("Pick Date & Time", color = MoltenOrange)
                    }
                    listOf("+1h" to 3_600_000L, "+3h" to 10_800_000L, "+24h" to 86_400_000L).forEach { (lbl, offset) ->
                        OutlinedButton(
                            onClick = { matchTimeMillis = System.currentTimeMillis() + offset },
                            border = BorderStroke(1.dp, ArenaOutline)
                        ) {
                            Text(lbl, color = TextSecondary)
                        }
                    }
                }
            }

            ArenaTextField(prizeDetails, { prizeDetails = it }, "Prize & Recognition Details *")
            ArenaTextField(rulesText, { rulesText = it }, "Tournament Rules *", singleLine = false, minLines = 3)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Feature on Home Banner", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Switch(
                    checked = featured,
                    onCheckedChange = { featured = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = NeonCrimson)
                )
            }

            if (existing == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Broadcast New Tournament Alert", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Switch(
                        checked = broadcast,
                        onCheckedChange = { broadcast = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = MoltenOrange)
                    )
                }
            }

            if (errorMsg != null) {
                Text(errorMsg!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val slots = maxSlotsText.toLongOrNull()
                    val err = when {
                        title.trim().length < 3 -> "Title must be at least 3 characters."
                        slots == null || slots < 2L || slots > 100L -> "Max slots must be between 2 and 100."
                        prizeDetails.trim().length < 2 -> "Enter prize details."
                        rulesText.trim().length < 5 -> "Enter tournament rules."
                        else -> null
                    }
                    if (err != null) {
                        errorMsg = err
                    } else {
                        onSave(
                            title.trim(),
                            mode.name,
                            category.name,
                            status.name,
                            bannerKey.key,
                            mapName.trim().ifBlank { "Bermuda" },
                            matchTimeMillis,
                            matchTimeMillis - 900_000L, // Room opens 15 mins prior
                            slots!!,
                            prizeDetails.trim(),
                            rulesText.trim(),
                            featured,
                            broadcast
                        )
                    }
                },
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admin_save_tournament_button")
            ) {
                if (isBusy) CircularProgressIndicator(Modifier.size(20.dp), color = TextPrimary, strokeWidth = 2.dp)
                else Text(if (existing == null) "Publish Tournament" else "Save Changes", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinnerAnnouncementSheet(
    tournament: Tournament,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onPublish: (winner: String, runnerUp: String, third: String, mvp: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var winner by remember { mutableStateOf(tournament.winnerName ?: "") }
    var runnerUp by remember { mutableStateOf(tournament.runnerUpName ?: "") }
    var third by remember { mutableStateOf(tournament.thirdPlaceName ?: "") }
    var mvp by remember { mutableStateOf(tournament.mvpName ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ArenaSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ANNOUNCE TOURNAMENT WINNERS", style = MaterialTheme.typography.headlineSmall, color = ChampionshipGold)
            Text(tournament.title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            ArenaTextField(winner, { winner = it }, "1st Place / Champion Team or IGN *")
            ArenaTextField(runnerUp, { runnerUp = it }, "2nd Place Runner-Up (Optional)")
            ArenaTextField(third, { third = it }, "3rd Place (Optional)")
            ArenaTextField(mvp, { mvp = it }, "MVP Player IGN (Optional)")
            Button(
                onClick = { if (winner.trim().isNotEmpty()) onPublish(winner, runnerUp, third, mvp) },
                enabled = !isBusy && winner.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = ChampionshipGold, contentColor = com.example.ui.theme.ArenaBlack),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Crown Winners & Broadcast", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardResultSheet(
    tournament: Tournament,
    approvedRegs: List<Registration>,
    existingEntry: LeaderboardEntry?,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        userId: String,
        playerName: String,
        ign: String,
        teamName: String,
        ffUid: String,
        kills: Long,
        rank: Long,
        posPts: Long,
        badge: WinnerBadge
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var userId by remember { mutableStateOf(existingEntry?.userId ?: "") }
    var playerName by remember { mutableStateOf(existingEntry?.playerName ?: "") }
    var ign by remember { mutableStateOf(existingEntry?.ign ?: "") }
    var teamName by remember { mutableStateOf(existingEntry?.teamName ?: "") }
    var ffUid by remember { mutableStateOf(existingEntry?.freeFireUid ?: "") }
    var killsText by remember { mutableStateOf((existingEntry?.kills ?: 0L).toString()) }
    var rankText by remember { mutableStateOf((existingEntry?.placementRank ?: 1L).toString()) }
    var posPtsText by remember {
        mutableStateOf((existingEntry?.positionPoints ?: ArenaViewModel.defaultPlacementPoints(1)).toString())
    }
    var badge by remember { mutableStateOf(existingEntry?.badgeEnum ?: WinnerBadge.CHAMPION) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ArenaSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("UPDATE MATCH RESULT", style = MaterialTheme.typography.headlineSmall, color = MoltenOrange)
            Text(tournament.title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

            if (existingEntry == null && approvedRegs.isNotEmpty()) {
                Text("QUICK FILL FROM APPROVED PARTICIPANTS", style = MaterialTheme.typography.labelSmall, color = LiveEmerald)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    approvedRegs.forEach { reg ->
                        FilterChip(
                            selected = userId == reg.userId,
                            onClick = {
                                userId = reg.userId
                                playerName = reg.playerName
                                ign = reg.ign
                                teamName = reg.teamName
                                ffUid = reg.freeFireUid
                            },
                            label = { Text("${reg.teamName} (${reg.ign})") },
                            colors = adminChipColors(LiveEmerald)
                        )
                    }
                }
            }

            ArenaTextField(playerName, { playerName = it }, "Player Name *")
            ArenaTextField(ign, { ign = it }, "In-Game Name (IGN) *")
            ArenaTextField(teamName, { teamName = it }, "Team Name *")
            ArenaTextField(ffUid, { ffUid = it }, "Free Fire UID *", keyboardType = KeyboardType.Number)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArenaTextField(
                    value = rankText,
                    onValueChange = {
                        rankText = it
                        val r = it.toIntOrNull()
                        if (r != null) {
                            posPtsText = ArenaViewModel.defaultPlacementPoints(r).toString()
                            badge = when (r) {
                                1 -> WinnerBadge.CHAMPION
                                2 -> WinnerBadge.RUNNER_UP
                                3 -> WinnerBadge.THIRD
                                else -> WinnerBadge.NONE
                            }
                        }
                    },
                    label = "Rank (1-100)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                ArenaTextField(
                    value = killsText,
                    onValueChange = { killsText = it },
                    label = "Kills",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                ArenaTextField(
                    value = posPtsText,
                    onValueChange = { posPtsText = it },
                    label = "Pos Pts",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WinnerBadge.entries.forEach { b ->
                    FilterChip(
                        selected = badge == b,
                        onClick = { badge = b },
                        label = { Text(b.label) },
                        colors = adminChipColors(ChampionshipGold)
                    )
                }
            }

            val previewTotal = (killsText.toLongOrNull() ?: 0L) + (posPtsText.toLongOrNull() ?: 0L)
            Text("Total Points: $previewTotal", style = MaterialTheme.typography.titleMedium, color = LiveEmerald)

            Button(
                onClick = {
                    val kills = (killsText.toLongOrNull() ?: 0L).coerceAtLeast(0L)
                    val rank = (rankText.toLongOrNull() ?: 1L).coerceIn(1L, 100L)
                    val posPts = (posPtsText.toLongOrNull() ?: 0L).coerceAtLeast(0L)
                    onSave(userId, playerName, ign, teamName, ffUid, kills, rank, posPts, badge)
                },
                enabled = !isBusy && playerName.trim().length >= 2 && ign.trim().length >= 2 && ffUid.trim().length >= 4,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isBusy) CircularProgressIndicator(Modifier.size(20.dp), color = TextPrimary, strokeWidth = 2.dp)
                else Text("Save Match Result", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun adminChipColors(accent: androidx.compose.ui.graphics.Color = NeonCrimson) =
    FilterChipDefaults.filterChipColors(
        containerColor = ArenaCharcoal,
        labelColor = TextSecondary,
        selectedContainerColor = accent.copy(alpha = 0.22f),
        selectedLabelColor = accent
    )

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LeaderboardEntry
import com.example.data.model.PrivatePlayer
import com.example.data.model.PublicPlayer
import com.example.data.model.Registration
import com.example.data.model.RoomAccess
import com.example.data.model.Tournament
import com.example.data.model.TournamentStatus
import com.example.ui.components.EsportsCard
import com.example.ui.components.PlayerRegistrationSheet
import com.example.ui.components.RegistrationConfirmationDialog
import com.example.ui.components.RegistrationStatusPill
import com.example.ui.components.RoomCredentialsCard
import com.example.ui.components.StatusPill
import com.example.ui.components.TournamentStatusPill
import com.example.ui.components.bannerDrawableRes
import com.example.ui.components.formatMatchDateTime
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.ArenaCharcoal
import com.example.ui.theme.ArenaOutline
import com.example.ui.theme.ArenaSurface
import com.example.ui.theme.ArenaSurfaceElevated
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LiveEmerald
import com.example.ui.theme.MoltenOrange
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCrimsonSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentDetailScreen(
    tournament: Tournament?,
    myRegistration: Registration?,
    publicProfile: PublicPlayer?,
    privateProfile: PrivatePlayer?,
    googleDisplayName: String,
    isAdmin: Boolean,
    roomAccess: RoomAccess?,
    tournamentLeaderboard: List<LeaderboardEntry>,
    nowMillis: Long,
    isBusy: Boolean,
    onSelectForRoom: (String?) -> Unit,
    onSubmitRegistration: (
        tournament: Tournament,
        playerName: String,
        ign: String,
        freeFireUid: String,
        whatsapp: String,
        teamName: String,
        teammateIgns: List<String>,
        teammateUids: List<String>,
        onConfirmed: () -> Unit
    ) -> Unit,
    onCopiedMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showRegisterSheet by remember { mutableStateOf(false) }
    var showConfirmedDialog by remember { mutableStateOf(false) }

    DisposableEffect(tournament?.id) {
        onSelectForRoom(tournament?.id)
        onDispose { onSelectForRoom(null) }
    }

    Scaffold(
        containerColor = ArenaBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ArenaCharcoal, titleContentColor = TextPrimary),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                title = {
                    Text(
                        text = tournament?.title ?: "Tournament Lobby",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )
                }
            )
        }
    ) { innerPadding ->
        if (tournament == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Tournament not found or removed.", color = TextSecondary)
            }
            return@Scaffold
        }

        val slotProgress = if (tournament.maxSlots > 0) {
            (tournament.registeredSlots.toFloat() / tournament.maxSlots.toFloat()).coerceIn(0f, 1f)
        } else 0f

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner
            item {
                EsportsCard(highlightColor = NeonCrimson.copy(alpha = 0.5f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) {
                        Image(
                            painter = painterResource(id = bannerDrawableRes(tournament.bannerKey)),
                            contentDescription = tournament.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.2f), ArenaBlack.copy(alpha = 0.94f))
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TournamentStatusPill(tournament.statusEnum)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusPill(tournament.modeEnum.label, ArenaBlack.copy(alpha = 0.8f), TextPrimary)
                                StatusPill("FREE ENTRY", NeonCrimsonSoft, NeonCrimson)
                            }
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(tournament.title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                            Text(
                                "${tournament.categoryEnum.label} • Map: ${tournament.mapName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MoltenOrange
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InfoLine(Icons.Filled.Schedule, "Match Time", formatMatchDateTime(tournament.matchTimeMillis))
                        InfoLine(Icons.Filled.Map, "Room Opens", formatMatchDateTime(tournament.roomOpenTimeMillis))
                        InfoLine(Icons.Filled.EmojiEvents, "Prize Details", tournament.prizeDetails, ChampionshipGold)
                        InfoLine(
                            Icons.Filled.Groups,
                            "Slots",
                            "${tournament.registeredSlots} / ${tournament.maxSlots} Registered (${tournament.availableSlots} available)"
                        )
                        LinearProgressIndicator(
                            progress = { slotProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonCrimson,
                            trackColor = ArenaSurfaceElevated
                        )
                    }
                }
            }

            // Registration CTA or Status
            item {
                if (myRegistration != null) {
                    EsportsCard(highlightColor = MoltenOrange.copy(alpha = 0.5f)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("YOUR REGISTRATION", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                RegistrationStatusPill(myRegistration.statusEnum)
                            }
                            Text(
                                text = "Team: ${myRegistration.teamName} • Captain IGN: ${myRegistration.ign} (UID: ${myRegistration.freeFireUid})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            if (myRegistration.teammateIgns.isNotEmpty()) {
                                Text(
                                    text = "Roster: ${myRegistration.teammateIgns.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MoltenOrange
                                )
                            }
                            if (!myRegistration.adminNote.isNullOrBlank()) {
                                Text(
                                    text = "Admin Note: ${myRegistration.adminNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ChampionshipGold
                                )
                            }
                        }
                    }
                } else if (tournament.statusEnum != TournamentStatus.COMPLETED) {
                    Button(
                        onClick = { showRegisterSheet = true },
                        enabled = tournament.availableSlots > 0L,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("detail_register_now_button")
                    ) {
                        Text(
                            text = if (tournament.availableSlots > 0L) "Register Now (Free Entry)" else "Tournament Full",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Room Details Card (Protected!)
            item {
                RoomCredentialsCard(
                    tournament = tournament,
                    registration = myRegistration,
                    isAdmin = isAdmin,
                    roomAccess = roomAccess,
                    nowMillis = nowMillis,
                    onCopyText = { label, value ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
                        onCopiedMessage("$label copied to clipboard!")
                    }
                )
            }

            // Winner Podium if Completed or Winner Announced
            if (!tournament.winnerName.isNullOrBlank()) {
                item {
                    EsportsCard(highlightColor = ChampionshipGold.copy(alpha = 0.7f)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = ChampionshipGold)
                                Text("OFFICIAL WINNERS", style = MaterialTheme.typography.titleMedium, color = ChampionshipGold)
                            }
                            Text("1st Champion: ${tournament.winnerName}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            if (!tournament.runnerUpName.isNullOrBlank()) {
                                Text("2nd Runner-Up: ${tournament.runnerUpName}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                            if (!tournament.thirdPlaceName.isNullOrBlank()) {
                                Text("3rd Place: ${tournament.thirdPlaceName}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                            if (!tournament.mvpName.isNullOrBlank()) {
                                Text("Tournament MVP: ${tournament.mvpName}", style = MaterialTheme.typography.bodyMedium, color = MoltenOrange)
                            }
                        }
                    }
                }
            }

            // Tournament Rules
            item {
                EsportsCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Gavel, contentDescription = null, tint = MoltenOrange)
                            Text("TOURNAMENT RULES", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        HorizontalDivider(color = ArenaOutline)
                        Text(
                            text = tournament.rulesText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Tournament Standings Preview
            if (tournamentLeaderboard.isNotEmpty()) {
                item {
                    Text(
                        text = "MATCH STANDINGS",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
                items(tournamentLeaderboard.sortedByDescending { it.totalPoints }, key = { it.id }) { entry ->
                    Surface(
                        color = ArenaSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ArenaOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "#${entry.placementRank}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = if (entry.placementRank == 1L) ChampionshipGold else MoltenOrange
                                )
                                Column {
                                    Text(entry.teamName, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text("${entry.ign} • ${entry.kills} Kills", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Text(
                                text = "${entry.totalPoints} PTS",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = LiveEmerald
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRegisterSheet && tournament != null) {
        PlayerRegistrationSheet(
            tournament = tournament,
            publicProfile = publicProfile,
            privateProfile = privateProfile,
            defaultName = googleDisplayName,
            isBusy = isBusy,
            onDismiss = { showRegisterSheet = false },
            onSubmit = { name, ign, uid, wa, team, mateIgns, mateUids ->
                onSubmitRegistration(tournament, name, ign, uid, wa, team, mateIgns, mateUids) {
                    showRegisterSheet = false
                    showConfirmedDialog = true
                }
            }
        )
    }

    if (showConfirmedDialog && tournament != null) {
        RegistrationConfirmationDialog(
            tournamentTitle = tournament.title,
            onDismiss = { showConfirmedDialog = false }
        )
    }
}

@Composable
private fun InfoLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MoltenOrange, modifier = Modifier.size(18.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
        }
    }
}

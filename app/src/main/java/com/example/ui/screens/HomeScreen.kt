package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Announcement
import com.example.data.model.PublicPlayer
import com.example.data.model.Registration
import com.example.data.model.Tournament
import com.example.data.model.TournamentStatus
import com.example.ui.components.EsportsCard
import com.example.ui.components.StatusPill
import com.example.ui.components.TournamentCard
import com.example.ui.components.bannerDrawableRes
import com.example.ui.components.formatCountdown
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
import com.example.ui.theme.NeonCrimsonSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    tournamentsState: UiState<List<Tournament>>,
    playersState: UiState<List<PublicPlayer>>,
    myProfile: PublicPlayer?,
    displayName: String,
    myRegistrations: List<Registration>,
    announcements: List<Announcement>,
    nowMillis: Long,
    isAdmin: Boolean,
    onOpenTournament: (String) -> Unit,
    onBrowseAllTournaments: () -> Unit,
    onOpenAdmin: () -> Unit,
    onSetupProfile: () -> Unit,
    onOpenAnnouncements: () -> Unit
) {
    val tournaments = (tournamentsState as? UiState.Success)?.data ?: emptyList()
    val playersCount = (playersState as? UiState.Success)?.data?.size ?: 0
    val regMap = myRegistrations.associateBy { it.tournamentId }

    val liveTournaments = tournaments.filter { it.statusEnum == TournamentStatus.LIVE }
    val upcomingTournaments = tournaments.filter { it.statusEnum == TournamentStatus.UPCOMING }
    val completedTournaments = tournaments.filter { it.statusEnum == TournamentStatus.COMPLETED }
    val featuredTournament = tournaments.firstOrNull { it.featured && it.statusEnum != TournamentStatus.COMPLETED }
        ?: liveTournaments.firstOrNull()
        ?: upcomingTournaments.firstOrNull()
        ?: tournaments.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Welcome Header Card with App Logo
        item {
            EsportsCard(
                highlightColor = NeonCrimson.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(ArenaSurface, NeonCrimson.copy(alpha = 0.14f))
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, MoltenOrange, RoundedCornerShape(14.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_ffarena_logo),
                            contentDescription = "FF Arena Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WELCOME TO FF ARENA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MoltenOrange
                        )
                        Text(
                            text = myProfile?.ign?.ifBlank { null } ?: displayName.ifBlank { "Survivor" },
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (myProfile != null) "UID: ${myProfile.freeFireUid} • ${myProfile.totalWins} Wins" else "Complete your Free Fire UID to join matches fast",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    if (myProfile == null) {
                        Button(
                            onClick = onSetupProfile,
                            colors = ButtonDefaults.buttonColors(containerColor = MoltenOrange, contentColor = ArenaBlack),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("home_setup_profile_btn")
                        ) {
                            Text("Set UID", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Stats Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCounterTile(
                    icon = Icons.Filled.LiveTv,
                    label = "LIVE",
                    value = liveTournaments.size.toString(),
                    accent = LiveEmerald,
                    modifier = Modifier.weight(1f)
                )
                StatCounterTile(
                    icon = Icons.Filled.Schedule,
                    label = "UPCOMING",
                    value = upcomingTournaments.size.toString(),
                    accent = MoltenOrange,
                    modifier = Modifier.weight(1f)
                )
                StatCounterTile(
                    icon = Icons.Filled.EmojiEvents,
                    label = "COMPLETED",
                    value = completedTournaments.size.toString(),
                    accent = ChampionshipGold,
                    modifier = Modifier.weight(1f)
                )
                StatCounterTile(
                    icon = Icons.Filled.Groups,
                    label = "PLAYERS",
                    value = playersCount.toString(),
                    accent = NeonCrimson,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Featured Tournament Banner
        if (featuredTournament != null) {
            item {
                SectionHeader(title = "FEATURED TOURNAMENT", actionLabel = "All Events", onAction = onBrowseAllTournaments)
                Spacer(Modifier.height(8.dp))
                EsportsCard(
                    highlightColor = MoltenOrange.copy(alpha = 0.7f),
                    onClick = { onOpenTournament(featuredTournament.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("featured_tournament_banner")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) {
                        Image(
                            painter = painterResource(id = bannerDrawableRes(featuredTournament.bannerKey)),
                            contentDescription = featuredTournament.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.15f), ArenaBlack.copy(alpha = 0.94f))
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusPill("FEATURED • ${featuredTournament.modeEnum.label}", NeonCrimsonSoft, NeonCrimson)
                            StatusPill("FREE ENTRY", ArenaBlack.copy(alpha = 0.8f), ChampionshipGold)
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = featuredTournament.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${featuredTournament.categoryEnum.label} • ${featuredTournament.mapName} • ${formatMatchDateTime(featuredTournament.matchTimeMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (featuredTournament.matchTimeMillis > nowMillis) {
                                        "Starts in ${formatCountdown(featuredTournament.matchTimeMillis, nowMillis)}"
                                    } else {
                                        featuredTournament.prizeDetails
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = MoltenOrange
                                )
                                Button(
                                    onClick = { onOpenTournament(featuredTournament.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Enter Lobby", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Announcements Strip
        item {
            SectionHeader(
                title = "TOURNAMENT ANNOUNCEMENTS",
                actionLabel = if (announcements.isNotEmpty()) "View All" else null,
                onAction = onOpenAnnouncements
            )
            Spacer(Modifier.height(8.dp))
            if (announcements.isEmpty()) {
                EsportsCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = MoltenOrange)
                        Text(
                            text = "No announcements yet. Tournament alerts & room drops appear here in real time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(announcements.take(5), key = { it.id }) { ann ->
                        EsportsCard(
                            highlightColor = MoltenOrange.copy(alpha = 0.45f),
                            onClick = {
                                val tid = ann.tournamentId
                                if (!tid.isNullOrBlank()) onOpenTournament(tid) else onOpenAnnouncements()
                            },
                            modifier = Modifier.width(280.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StatusPill(ann.typeEnum.label, MoltenOrange.copy(alpha = 0.16f), MoltenOrange)
                                Text(
                                    text = ann.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = ann.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live & Upcoming Tournaments Sections
        when (tournamentsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonCrimson)
                    }
                }
            }

            is UiState.Error -> {
                item {
                    EsportsCard(highlightColor = ErrorRed.copy(alpha = 0.6f)) {
                        Text(
                            text = tournamentsState.message,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            is UiState.Success -> {
                if (tournaments.isEmpty()) {
                    item {
                        EsportsCard(
                            highlightColor = ArenaOutline,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = MoltenOrange, modifier = Modifier.size(44.dp))
                                Text(
                                    text = "No Tournaments Live Yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isAdmin) {
                                        "You are signed in as Admin. Open the Admin Command Center to create your first real Free Fire tournament."
                                    } else {
                                        "Tournaments published by the Admin will appear here automatically in real time."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                if (isAdmin) {
                                    Button(
                                        onClick = onOpenAdmin,
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                                        modifier = Modifier.testTag("home_admin_create_btn")
                                    ) {
                                        Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Open Admin Panel")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (liveTournaments.isNotEmpty()) {
                        item {
                            SectionHeader(title = "LIVE TOURNAMENTS", actionLabel = "See All", onAction = onBrowseAllTournaments)
                        }
                        items(liveTournaments, key = { "live_${it.id}" }) { tournament ->
                            TournamentCard(
                                tournament = tournament,
                                myRegistration = regMap[tournament.id],
                                nowMillis = nowMillis,
                                onClick = { onOpenTournament(tournament.id) }
                            )
                        }
                    }

                    if (upcomingTournaments.isNotEmpty()) {
                        item {
                            SectionHeader(title = "UPCOMING TOURNAMENTS", actionLabel = "See All", onAction = onBrowseAllTournaments)
                        }
                        items(upcomingTournaments.take(5), key = { "up_${it.id}" }) { tournament ->
                            TournamentCard(
                                tournament = tournament,
                                myRegistration = regMap[tournament.id],
                                nowMillis = nowMillis,
                                onClick = { onOpenTournament(tournament.id) }
                            )
                        }
                    }

                    if (completedTournaments.isNotEmpty()) {
                        item {
                            SectionHeader(title = "RECENT COMPLETED MATCHES", actionLabel = "See All", onAction = onBrowseAllTournaments)
                        }
                        items(completedTournaments.take(3), key = { "comp_${it.id}" }) { tournament ->
                            TournamentCard(
                                tournament = tournament,
                                myRegistration = regMap[tournament.id],
                                nowMillis = nowMillis,
                                onClick = { onOpenTournament(tournament.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCounterTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ArenaSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ArenaOutline)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = JetBrainsMonoFontFamily,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .background(NeonCrimson, RoundedCornerShape(2.dp))
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = MoltenOrange, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

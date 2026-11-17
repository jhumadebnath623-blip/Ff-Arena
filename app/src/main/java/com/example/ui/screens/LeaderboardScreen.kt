package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.LeaderboardEntry
import com.example.data.model.PublicPlayer
import com.example.data.model.Tournament
import com.example.ui.components.EsportsCard
import com.example.ui.components.StatusPill
import com.example.ui.state.UiState
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.ArenaCharcoal
import com.example.ui.theme.ArenaOutline
import com.example.ui.theme.ArenaSurface
import com.example.ui.theme.BronzeMedal
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LiveEmerald
import com.example.ui.theme.MoltenOrange
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.SilverMedal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LeaderboardScreen(
    leaderboardState: UiState<List<LeaderboardEntry>>,
    playersState: UiState<List<PublicPlayer>>,
    tournamentsState: UiState<List<Tournament>>
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Players, 1=Teams, 2=Winners
    val tabs = listOf("Players", "Teams", "Winners")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBlack)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = ArenaCharcoal,
            contentColor = NeonCrimson,
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
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == idx) NeonCrimson else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("leaderboard_tab_$label")
                )
            }
        }

        when (selectedTab) {
            0 -> PlayerRankingsTab(playersState, leaderboardState)
            1 -> TeamRankingsTab(leaderboardState)
            2 -> WinnersHallOfFameTab(tournamentsState, leaderboardState)
        }
    }
}

@Composable
private fun PlayerRankingsTab(
    playersState: UiState<List<PublicPlayer>>,
    leaderboardState: UiState<List<LeaderboardEntry>>
) {
    val players = (playersState as? UiState.Success)?.data ?: emptyList()
    val entries = (leaderboardState as? UiState.Success)?.data ?: emptyList()

    // Merge career stats from public_players; fallback to leaderboard aggregation if player profiles not yet created
    val rankedPlayers = remember(players, entries) {
        if (players.any { it.totalPoints > 0 || it.totalKills > 0 }) {
            players.sortedWith(
                compareByDescending<PublicPlayer> { it.totalPoints }
                    .thenByDescending { it.totalWins }
                    .thenByDescending { it.totalKills }
            )
        } else {
            players.sortedByDescending { it.totalPoints }
        }
    }

    if (playersState is UiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NeonCrimson)
        }
        return
    }
    if (playersState is UiState.Error) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            Text(playersState.message, color = ErrorRed)
        }
        return
    }

    if (rankedPlayers.isEmpty()) {
        EmptyLeaderboardState("No ranked players yet. Standings update automatically once matches are scored!")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(rankedPlayers, key = { _, p -> p.userId }) { index, player ->
            val rank = index + 1
            val rankColor = rankAccentColor(rank)
            Surface(
                color = ArenaSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (rank <= 3) rankColor.copy(alpha = 0.6f) else ArenaOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        RankCircle(rank = rank, color = rankColor)
                        Column {
                            Text(
                                text = player.ign,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${player.playerName} • UID ${player.freeFireUid}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${player.totalWins} Wins • ${player.totalKills} Kills • ${player.matchesPlayed} Matches",
                                style = MaterialTheme.typography.labelSmall,
                                color = MoltenOrange
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${player.totalPoints}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = LiveEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text("PTS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }
    }
}

private data class TeamAggregate(
    val teamName: String,
    val kills: Long,
    val positionPoints: Long,
    val totalPoints: Long,
    val matches: Int,
    val championships: Int
)

@Composable
private fun TeamRankingsTab(leaderboardState: UiState<List<LeaderboardEntry>>) {
    when (leaderboardState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCrimson)
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                Text(leaderboardState.message, color = ErrorRed)
            }
        }
        is UiState.Success -> {
            val aggregated = remember(leaderboardState.data) {
                leaderboardState.data
                    .groupBy { it.teamName.ifBlank { it.ign } }
                    .map { (team, list) ->
                        TeamAggregate(
                            teamName = team,
                            kills = list.sumOf { it.kills },
                            positionPoints = list.sumOf { it.positionPoints },
                            totalPoints = list.sumOf { it.totalPoints },
                            matches = list.size,
                            championships = list.count { it.placementRank == 1L }
                        )
                    }
                    .sortedWith(
                        compareByDescending<TeamAggregate> { it.totalPoints }
                            .thenByDescending { it.kills }
                    )
            }

            if (aggregated.isEmpty()) {
                EmptyLeaderboardState("No team match results posted yet. Admin results automatically update team standings here.")
                return
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(aggregated, key = { _, t -> t.teamName }) { index, team ->
                    val rank = index + 1
                    val rankColor = rankAccentColor(rank)
                    Surface(
                        color = ArenaSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (rank <= 3) rankColor.copy(alpha = 0.6f) else ArenaOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                RankCircle(rank = rank, color = rankColor)
                                Column {
                                    Text(team.teamName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                    Text(
                                        text = "Kills: ${team.kills} • Pos Pts: ${team.positionPoints} • Booyahs: ${team.championships}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${team.totalPoints}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = MoltenOrange,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("TOTAL", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WinnersHallOfFameTab(
    tournamentsState: UiState<List<Tournament>>,
    leaderboardState: UiState<List<LeaderboardEntry>>
) {
    val tournaments = (tournamentsState as? UiState.Success)?.data ?: emptyList()
    val completedWithWinners = tournaments.filter { !it.winnerName.isNullOrBlank() }
    val matchEntries = (leaderboardState as? UiState.Success)?.data ?: emptyList()

    if (completedWithWinners.isEmpty() && matchEntries.isEmpty()) {
        EmptyLeaderboardState("No tournament champions announced yet. Winners will be showcased in the Hall of Fame.")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        itemsIndexed(completedWithWinners, key = { _, t -> t.id }) { _, t ->
            EsportsCard(highlightColor = ChampionshipGold.copy(alpha = 0.65f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                        StatusPill(t.modeEnum.label, ArenaCharcoal, MoltenOrange)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = ChampionshipGold)
                        Text("1st Champion: ${t.winnerName}", style = MaterialTheme.typography.titleMedium, color = ChampionshipGold)
                    }
                    if (!t.runnerUpName.isNullOrBlank()) {
                        Text("2nd Runner-Up: ${t.runnerUpName}", style = MaterialTheme.typography.bodyMedium, color = SilverMedal)
                    }
                    if (!t.thirdPlaceName.isNullOrBlank()) {
                        Text("3rd Place: ${t.thirdPlaceName}", style = MaterialTheme.typography.bodyMedium, color = BronzeMedal)
                    }
                    if (!t.mvpName.isNullOrBlank()) {
                        Text("MVP: ${t.mvpName}", style = MaterialTheme.typography.labelLarge, color = NeonCrimson)
                    }
                }
            }
        }

        if (matchEntries.isNotEmpty()) {
            item {
                Text("LATEST MATCH SCORECARDS", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            }
            itemsIndexed(
                matchEntries.sortedByDescending { it.totalPoints },
                key = { _, e -> e.id }
            ) { _, entry ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.tournamentTitle, style = MaterialTheme.typography.labelSmall, color = MoltenOrange)
                            Text("${entry.teamName} (${entry.ign})", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text(
                                "Rank #${entry.placementRank} • Kills: ${entry.kills} • Pos Pts: ${entry.positionPoints}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            "${entry.totalPoints} PTS",
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

@Composable
private fun RankCircle(rank: Int, color: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelLarge,
            fontFamily = JetBrainsMonoFontFamily,
            color = color
        )
    }
}

private fun rankAccentColor(rank: Int): Color = when (rank) {
    1 -> ChampionshipGold
    2 -> SilverMedal
    3 -> BronzeMedal
    else -> TextSecondary
}

@Composable
private fun EmptyLeaderboardState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        EsportsCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.MilitaryTech, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(44.dp))
                Text("Leaderboard Ready", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}

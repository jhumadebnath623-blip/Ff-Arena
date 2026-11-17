package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Registration
import com.example.data.model.Tournament
import com.example.data.model.TournamentCategory
import com.example.data.model.TournamentMode
import com.example.data.model.TournamentStatus
import com.example.ui.components.EsportsCard
import com.example.ui.components.TournamentCard
import com.example.ui.state.UiState
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.ArenaCharcoal
import com.example.ui.theme.ArenaOutline
import com.example.ui.theme.ArenaSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MoltenOrange
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TournamentsScreen(
    tournamentsState: UiState<List<Tournament>>,
    myRegistrations: List<Registration>,
    nowMillis: Long,
    onOpenTournament: (String) -> Unit
) {
    var selectedStatusTab by remember { mutableIntStateOf(0) } // 0=All, 1=Upcoming, 2=Live, 3=Completed
    var selectedMode by remember { mutableStateOf<TournamentMode?>(null) }
    var selectedCategory by remember { mutableStateOf<TournamentCategory?>(null) }

    val regMap = remember(myRegistrations) { myRegistrations.associateBy { it.tournamentId } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBlack)
    ) {
        val statusTabs = listOf("All", "Upcoming", "Live", "Completed")
        TabRow(
            selectedTabIndex = selectedStatusTab,
            containerColor = ArenaCharcoal,
            contentColor = NeonCrimson,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabTab(selectedStatusTab)]),
                    color = NeonCrimson
                )
            }
        ) {
            statusTabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedStatusTab == idx,
                    onClick = { selectedStatusTab = idx },
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedStatusTab == idx) NeonCrimson else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tournaments_tab_$label")
                )
            }
        }

        // Mode & Category Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ArenaCharcoal)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.FilterList, contentDescription = null, tint = MoltenOrange, modifier = Modifier.size(18.dp))
                FilterChip(
                    selected = selectedMode == null,
                    onClick = { selectedMode = null },
                    label = { Text("All Modes") },
                    colors = arenaFilterChipColors(),
                    modifier = Modifier.testTag("filter_mode_all")
                )
                TournamentMode.entries.forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { selectedMode = if (selectedMode == mode) null else mode },
                        label = { Text(mode.label) },
                        colors = arenaFilterChipColors(),
                        modifier = Modifier.testTag("filter_mode_${mode.name}")
                    )
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All Categories") },
                    colors = arenaFilterChipColors(MoltenOrange),
                    modifier = Modifier.testTag("filter_cat_all")
                )
                TournamentCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(cat.label) },
                        colors = arenaFilterChipColors(MoltenOrange),
                        modifier = Modifier.testTag("filter_cat_${cat.name}")
                    )
                }
            }
        }

        when (tournamentsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonCrimson)
                }
            }

            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text(tournamentsState.message, color = ErrorRed)
                }
            }

            is UiState.Success -> {
                val filtered = tournamentsState.data.filter { t ->
                    val statusMatch = when (selectedStatusTab) {
                        1 -> t.statusEnum == TournamentStatus.UPCOMING
                        2 -> t.statusEnum == TournamentStatus.LIVE
                        3 -> t.statusEnum == TournamentStatus.COMPLETED
                        else -> true
                    }
                    val modeMatch = selectedMode == null || t.modeEnum == selectedMode
                    val catMatch = selectedCategory == null || t.categoryEnum == selectedCategory
                    statusMatch && modeMatch && catMatch
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        EsportsCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MoltenOrange, modifier = Modifier.size(40.dp))
                                Text("No Matching Tournaments", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                Text(
                                    "Try clearing your mode/category filters or check back when Admin schedules new brackets.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filtered, key = { it.id }) { tournament ->
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

private fun selectedTabTab(idx: Int) = idx

@Composable
private fun arenaFilterChipColors(accent: androidx.compose.ui.graphics.Color = NeonCrimson) =
    FilterChipDefaults.filterChipColors(
        containerColor = ArenaSurface,
        labelColor = TextSecondary,
        selectedContainerColor = accent.copy(alpha = 0.22f),
        selectedLabelColor = accent
    )

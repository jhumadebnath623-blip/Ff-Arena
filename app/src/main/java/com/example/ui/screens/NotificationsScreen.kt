package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Announcement
import com.example.data.model.UserNotification
import com.example.ui.components.EsportsCard
import com.example.ui.components.StatusPill
import com.example.ui.components.formatMatchDateTime
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.ArenaCharcoal
import com.example.ui.theme.ArenaOutline
import com.example.ui.theme.ArenaSurface
import com.example.ui.theme.MoltenOrange
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCrimsonSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<UserNotification>,
    announcements: List<Announcement>,
    initialTab: Int = 0,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onOpenTournament: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf("My Alerts (${notifications.count { !it.readFlag }})", "Announcements")

    Scaffold(
        containerColor = ArenaBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ArenaCharcoal, titleContentColor = TextPrimary),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notifications_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                title = { Text("NOTIFICATION CENTER", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    if (selectedTab == 0 && notifications.any { !it.readFlag }) {
                        TextButton(
                            onClick = onMarkAllRead,
                            modifier = Modifier.testTag("notifications_mark_all_read")
                        ) {
                            Icon(Icons.Filled.DoneAll, contentDescription = null, tint = MoltenOrange, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Read All", color = MoltenOrange)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedTab == idx) NeonCrimson else TextSecondary
                            )
                        }
                    )
                }
            }

            if (selectedTab == 0) {
                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        EsportsCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Notifications, contentDescription = null, tint = MoltenOrange, modifier = Modifier.size(40.dp))
                                Text("No Personal Alerts Yet", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                Text(
                                    "Registration confirmations, approval alerts, room drops, and match results appear here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            Surface(
                                color = ArenaSurface,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (!notif.readFlag) NeonCrimson.copy(alpha = 0.65f) else ArenaOutline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!notif.readFlag) onMarkRead(notif.id)
                                        notif.tournamentId?.takeIf { it.isNotBlank() }?.let(onOpenTournament)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatusPill(notif.typeEnum.label, NeonCrimsonSoft, NeonCrimson)
                                        if (!notif.readFlag) {
                                            Box(
                                                modifier = Modifier
                                                    .size(9.dp)
                                                    .clip(CircleShape)
                                                    .background(NeonCrimson)
                                            )
                                        }
                                    }
                                    Text(notif.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(notif.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    notif.createdAt?.toDate()?.time?.let {
                                        Text(formatMatchDateTime(it), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (announcements.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        EsportsCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Campaign, contentDescription = null, tint = MoltenOrange, modifier = Modifier.size(40.dp))
                                Text("No Announcements Yet", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                Text(
                                    "Global tournament updates and winner broadcasts will appear here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(announcements, key = { it.id }) { ann ->
                            EsportsCard(
                                highlightColor = MoltenOrange.copy(alpha = 0.45f),
                                onClick = {
                                    ann.tournamentId?.takeIf { it.isNotBlank() }?.let(onOpenTournament)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    StatusPill(ann.typeEnum.label, MoltenOrange.copy(alpha = 0.16f), MoltenOrange)
                                    Text(ann.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(ann.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    ann.createdAt?.toDate()?.time?.let {
                                        Text(formatMatchDateTime(it), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

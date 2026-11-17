package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.LeaderboardEntry
import com.example.data.model.PrivatePlayer
import com.example.data.model.PublicPlayer
import com.example.data.model.Registration
import com.example.data.remote.signOutUser
import com.example.ui.components.ArenaTextField
import com.example.ui.components.EsportsCard
import com.example.ui.components.RegistrationStatusPill
import com.example.ui.components.StatusPill
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.ArenaCharcoal
import com.example.ui.theme.ArenaOutline
import com.example.ui.theme.ArenaSurface
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LiveEmerald
import com.example.ui.theme.MoltenOrange
import com.example.ui.theme.MoltenOrangeSoft
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCrimsonSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    currentUserId: String,
    currentUserEmail: String,
    googleDisplayName: String,
    googlePhotoUrl: String?,
    publicProfile: PublicPlayer?,
    privateProfile: PrivatePlayer?,
    myRegistrations: List<Registration>,
    myMatchHistory: List<LeaderboardEntry>,
    isAdmin: Boolean,
    isBusy: Boolean,
    openEditorInitially: Boolean = false,
    onSaveProfile: (
        playerName: String,
        ign: String,
        freeFireUid: String,
        whatsappNumber: String,
        avatarKey: String,
        photoUrl: String?,
        onDone: () -> Unit
    ) -> Unit,
    onOpenTournament: (String) -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenSupport: () -> Unit,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showEditSheet by remember(openEditorInitially) { mutableStateOf(openEditorInitially) }
    var localPickedPhotoUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            localPickedPhotoUri = uri.toString()
        }
    }

    val effectivePhoto = localPickedPhotoUri ?: publicProfile?.photoUrl ?: googlePhotoUrl
    val achievements = remember(publicProfile) {
        val base = publicProfile?.achievements.orEmpty().toMutableList()
        if (base.isEmpty()) base.add("Arena Recruit")
        base
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Player Hero Card
        item {
            EsportsCard(highlightColor = NeonCrimson.copy(alpha = 0.55f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(2.dp, MoltenOrange, CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        ) {
                            if (!effectivePhoto.isNullOrBlank()) {
                                AsyncImage(
                                    model = effectivePhoto,
                                    contentDescription = "Player Avatar",
                                    contentScale = ContentScale.Crop,
                                    fallback = painterResource(id = R.drawable.img_default_avatar),
                                    error = painterResource(id = R.drawable.img_default_avatar),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_default_avatar),
                                    contentDescription = "Player Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = publicProfile?.ign?.ifBlank { null } ?: googleDisplayName.ifBlank { "Survivor" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextPrimary
                                )
                                if (isAdmin) {
                                    StatusPill("ADMIN", NeonCrimsonSoft, NeonCrimson)
                                }
                            }
                            Text(
                                text = publicProfile?.playerName ?: googleDisplayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = if (publicProfile != null) "FF UID: ${publicProfile.freeFireUid}" else "FF UID: Not configured",
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = MoltenOrange
                            )
                            Text(
                                text = currentUserEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showEditSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_edit_button")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (publicProfile == null) "Complete Profile" else "Edit Profile")
                        }
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            border = BorderStroke(1.dp, ArenaOutline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier.testTag("profile_pick_photo_button")
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = "Change Photo", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Career Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatBox("TOTAL WINS", (publicProfile?.totalWins ?: 0L).toString(), ChampionshipGold, Modifier.weight(1f))
                ProfileStatBox("TOTAL KILLS", (publicProfile?.totalKills ?: 0L).toString(), NeonCrimson, Modifier.weight(1f))
                ProfileStatBox("MATCHES", (publicProfile?.matchesPlayed ?: 0L).toString(), MoltenOrange, Modifier.weight(1f))
                ProfileStatBox("POINTS", (publicProfile?.totalPoints ?: 0L).toString(), LiveEmerald, Modifier.weight(1f))
            }
        }

        // Achievements
        item {
            EsportsCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.MilitaryTech, contentDescription = null, tint = ChampionshipGold)
                        Text("ACHIEVEMENTS", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        achievements.forEach { badge ->
                            Surface(
                                color = MoltenOrangeSoft,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, MoltenOrange.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(14.dp))
                                    Text(badge, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Registered Tournaments
        item {
            Text("REGISTERED TOURNAMENTS (${myRegistrations.size})", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }
        if (myRegistrations.isEmpty()) {
            item {
                EsportsCard {
                    Text(
                        text = "You haven't joined any tournaments yet. Browse the Tournaments tab to enter a free bracket!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(myRegistrations, key = { it.id }) { reg ->
                EsportsCard(
                    onClick = { onOpenTournament(reg.tournamentId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(reg.tournamentTitle, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text(
                                "${reg.modeEnum.label} • Team: ${reg.teamName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        RegistrationStatusPill(reg.statusEnum)
                    }
                }
            }
        }

        // Match History
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.History, contentDescription = null, tint = MoltenOrange)
                Text("MATCH HISTORY (${myMatchHistory.size})", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            }
        }
        if (myMatchHistory.isEmpty()) {
            item {
                EsportsCard {
                    Text(
                        text = "Completed match results and kill breakdowns will appear here automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(myMatchHistory, key = { it.id }) { item ->
                EsportsCard(onClick = { onOpenTournament(item.tournamentId) }) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.tournamentTitle, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text(
                                "Rank #${item.placementRank} • ${item.kills} Kills • ${item.positionPoints} Pos Pts",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (item.placementRank == 1L) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                "${item.totalPoints} PTS",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = LiveEmerald
                            )
                        }
                    }
                }
            }
        }

        // Admin Portal & Support & Sign Out
        item {
            EsportsCard {
                Column {
                    ProfileActionRow(
                        icon = Icons.Filled.Shield,
                        title = if (isAdmin) "Admin Command Center" else "Admin Portal",
                        subtitle = if (isAdmin) "Manage tournaments, rooms, approvals & results" else "Authorized tournament organizers only",
                        tint = NeonCrimson,
                        tag = "profile_admin_portal_row",
                        onClick = onOpenAdmin
                    )
                    HorizontalDivider(color = ArenaOutline)
                    ProfileActionRow(
                        icon = Icons.Filled.Help,
                        title = "Support, Rules, FAQ & Legal",
                        subtitle = "WhatsApp support, contact admin, rules, privacy & terms",
                        tint = MoltenOrange,
                        tag = "profile_support_row",
                        onClick = onOpenSupport
                    )
                    HorizontalDivider(color = ArenaOutline)
                    ProfileActionRow(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = "Sign Out",
                        subtitle = "UID: ${currentUserId.take(12)}...",
                        tint = ErrorRed,
                        tag = "profile_sign_out_row",
                        onClick = {
                            signOutUser(credentialManager, scope, onSignOutComplete = onSignedOut)
                        }
                    )
                }
            }
        }
    }

    if (showEditSheet) {
        EditProfileBottomSheet(
            publicProfile = publicProfile,
            privateProfile = privateProfile,
            defaultName = googleDisplayName,
            defaultPhoto = effectivePhoto,
            isBusy = isBusy,
            onDismiss = { showEditSheet = false },
            onSave = { name, ign, uid, wa ->
                onSaveProfile(name, ign, uid, wa, "cyber_red", effectivePhoto) {
                    showEditSheet = false
                }
            }
        )
    }
}

@Composable
private fun ProfileStatBox(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = ArenaSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ArenaOutline)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = JetBrainsMonoFontFamily,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    publicProfile: PublicPlayer?,
    privateProfile: PrivatePlayer?,
    defaultName: String,
    defaultPhoto: String?,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(privateProfile?.playerName ?: publicProfile?.playerName ?: defaultName) }
    var ign by remember { mutableStateOf(privateProfile?.ign ?: publicProfile?.ign ?: "") }
    var ffUid by remember { mutableStateOf(privateProfile?.freeFireUid ?: publicProfile?.freeFireUid ?: "") }
    var whatsapp by remember { mutableStateOf(privateProfile?.whatsappNumber ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }

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
            Text("ESPORTS PLAYER PROFILE", style = MaterialTheme.typography.headlineSmall, color = MoltenOrange)
            ArenaTextField(name, { name = it }, "Full Player Name *", modifier = Modifier.testTag("edit_profile_name"))
            ArenaTextField(ign, { ign = it }, "Free Fire IGN *", modifier = Modifier.testTag("edit_profile_ign"))
            ArenaTextField(
                ffUid,
                { ffUid = it },
                "Free Fire UID *",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.testTag("edit_profile_uid")
            )
            ArenaTextField(
                whatsapp,
                { whatsapp = it },
                "WhatsApp Number *",
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.testTag("edit_profile_whatsapp")
            )

            if (errorText != null) {
                Text(errorText!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val err = when {
                        name.trim().length < 2 -> "Enter your full name."
                        ign.trim().length < 2 -> "Enter your Free Fire IGN."
                        ffUid.trim().length < 4 -> "Free Fire UID must be at least 4 characters."
                        whatsapp.trim().length < 6 -> "Enter a valid WhatsApp number."
                        else -> null
                    }
                    if (err != null) errorText = err else onSave(name, ign, ffUid, whatsapp)
                },
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button")
            ) {
                if (isBusy) CircularProgressIndicator(Modifier.size(20.dp), color = TextPrimary, strokeWidth = 2.dp)
                else Text("Save Profile", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

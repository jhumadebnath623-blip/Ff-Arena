package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BannerKey
import com.example.data.model.PrivatePlayer
import com.example.data.model.PublicPlayer
import com.example.data.model.Registration
import com.example.data.model.RegistrationStatus
import com.example.data.model.RoomAccess
import com.example.data.model.Tournament
import com.example.data.model.TournamentMode
import com.example.data.model.TournamentStatus
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.ArenaCharcoal
import com.example.ui.theme.ArenaOutline
import com.example.ui.theme.ArenaSurface
import com.example.ui.theme.ArenaSurfaceElevated
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LiveEmerald
import com.example.ui.theme.LiveEmeraldSoft
import com.example.ui.theme.MoltenOrange
import com.example.ui.theme.MoltenOrangeSoft
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCrimsonSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@DrawableRes
fun bannerDrawableRes(bannerKey: String): Int = when (BannerKey.fromRaw(bannerKey)) {
    BannerKey.BATTLE_ROYALE -> R.drawable.img_banner_battle_royale
    BannerKey.CLASH_SQUAD -> R.drawable.img_banner_clash_squad
    BannerKey.CHAMPIONSHIP -> R.drawable.img_banner_championship
}

fun formatMatchDateTime(millis: Long): String {
    if (millis <= 0L) return "TBA"
    val sdf = SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatCountdown(targetMillis: Long, nowMillis: Long): String {
    val diff = targetMillis - nowMillis
    if (diff <= 0L) return "OPEN NOW"
    val totalSecs = diff / 1000L
    val days = totalSecs / 86400L
    val hours = (totalSecs % 86400L) / 3600L
    val mins = (totalSecs % 3600L) / 60L
    val secs = totalSecs % 60L
    return if (days > 0) {
        String.format(Locale.US, "%dd %02dh %02dm", days, hours, mins)
    } else {
        String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FFArenaTopBar(
    title: String,
    unreadCount: Int,
    isAdmin: Boolean,
    onNotificationsClick: () -> Unit,
    onSupportClick: () -> Unit,
    onAdminClick: () -> Unit,
    navigationIcon: (@Composable () -> Unit)? = null
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ArenaCharcoal,
            titleContentColor = TextPrimary
        ),
        navigationIcon = navigationIcon ?: {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, end = 4.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, NeonCrimson.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ffarena_logo),
                    contentDescription = "FF Arena Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "FREE FIRE ESPORTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MoltenOrange
                )
            }
        },
        actions = {
            if (isAdmin) {
                IconButton(
                    onClick = onAdminClick,
                    modifier = Modifier.testTag("topbar_admin_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Admin Command Center",
                        tint = NeonCrimson
                    )
                }
            }
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("topbar_notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = NeonCrimson, contentColor = TextPrimary) {
                                Text(if (unreadCount > 9) "9+" else unreadCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary
                    )
                }
            }
            IconButton(
                onClick = onSupportClick,
                modifier = Modifier.testTag("topbar_support_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = "Rules & Support",
                    tint = TextSecondary
                )
            }
        }
    )
}

@Composable
fun EsportsCard(
    modifier: Modifier = Modifier,
    highlightColor: Color = ArenaOutline,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    Card(
        modifier = cardModifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = ArenaSurface),
        border = BorderStroke(1.dp, highlightColor)
    ) {
        Column(content = content)
    }
}

@Composable
fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    pulsing: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "pulseAlpha"
    )
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (pulsing) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .alpha(pulseAlpha)
                        .clip(CircleShape)
                        .background(contentColor)
                )
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TournamentStatusPill(status: TournamentStatus) {
    when (status) {
        TournamentStatus.LIVE -> StatusPill("LIVE", LiveEmeraldSoft, LiveEmerald, pulsing = true)
        TournamentStatus.UPCOMING -> StatusPill("UPCOMING", MoltenOrangeSoft, MoltenOrange)
        TournamentStatus.COMPLETED -> StatusPill("COMPLETED", ArenaSurfaceElevated, TextSecondary)
    }
}

@Composable
fun RegistrationStatusPill(status: RegistrationStatus) {
    when (status) {
        RegistrationStatus.APPROVED -> StatusPill("APPROVED", LiveEmeraldSoft, LiveEmerald)
        RegistrationStatus.PENDING -> StatusPill("PENDING", MoltenOrangeSoft, MoltenOrange)
        RegistrationStatus.REJECTED -> StatusPill("REJECTED", NeonCrimsonSoft, ErrorRed)
    }
}

@Composable
fun TournamentCard(
    tournament: Tournament,
    myRegistration: Registration?,
    nowMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slotProgress = if (tournament.maxSlots > 0) {
        (tournament.registeredSlots.toFloat() / tournament.maxSlots.toFloat()).coerceIn(0f, 1f)
    } else 0f

    EsportsCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tournament_card_${tournament.id}"),
        highlightColor = if (tournament.statusEnum == TournamentStatus.LIVE) LiveEmerald.copy(alpha = 0.5f) else ArenaOutline,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
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
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                ArenaBlack.copy(alpha = 0.92f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TournamentStatusPill(tournament.statusEnum)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPill(tournament.modeEnum.label, ArenaBlack.copy(alpha = 0.78f), TextPrimary)
                    StatusPill("FREE ENTRY", NeonCrimsonSoft, NeonCrimson)
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = tournament.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${tournament.categoryEnum.label} • Map: ${tournament.mapName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MoltenOrange
                )
            }
        }

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Text(
                        text = formatMatchDateTime(tournament.matchTimeMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (tournament.statusEnum != TournamentStatus.COMPLETED && tournament.matchTimeMillis > nowMillis) {
                    Text(
                        text = formatCountdown(tournament.matchTimeMillis, nowMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MoltenOrange,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(16.dp))
                    Text(
                        text = tournament.prizeDetails,
                        style = MaterialTheme.typography.bodySmall,
                        color = ChampionshipGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Slots Filled: ${tournament.registeredSlots}/${tournament.maxSlots}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${tournament.availableSlots} left",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (tournament.availableSlots > 0) LiveEmerald else ErrorRed
                    )
                }
                LinearProgressIndicator(
                    progress = { slotProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (tournament.availableSlots > 0) NeonCrimson else ErrorRed,
                    trackColor = ArenaSurfaceElevated
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (myRegistration != null) {
                    RegistrationStatusPill(myRegistration.statusEnum)
                } else {
                    Text(
                        text = "Tap for rules & entry",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (myRegistration != null) ArenaSurfaceElevated else NeonCrimson
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("tournament_open_btn_${tournament.id}")
                ) {
                    Text(
                        text = when {
                            myRegistration != null -> "View Details"
                            tournament.statusEnum == TournamentStatus.COMPLETED -> "Results"
                            tournament.availableSlots <= 0L -> "Full"
                            else -> "Register Now"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun RoomCredentialsCard(
    tournament: Tournament,
    registration: Registration?,
    isAdmin: Boolean,
    roomAccess: RoomAccess?,
    nowMillis: Long,
    onCopyText: (label: String, value: String) -> Unit
) {
    val isApproved = isAdmin || registration?.statusEnum == RegistrationStatus.APPROVED

    EsportsCard(
        highlightColor = if (isApproved && roomAccess != null) LiveEmerald.copy(alpha = 0.6f) else ArenaOutline,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("room_credentials_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (isApproved) Icons.Filled.Bolt else Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (isApproved) MoltenOrange else TextSecondary
                    )
                    Text(
                        text = "CUSTOM ROOM LOBBY",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                if (isApproved) {
                    StatusPill("AUTHORIZED", LiveEmeraldSoft, LiveEmerald)
                } else {
                    StatusPill("LOCKED", NeonCrimsonSoft, NeonCrimson)
                }
            }

            // Live Dual Countdown Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CountdownTile(
                    label = "ROOM OPENS IN",
                    value = formatCountdown(tournament.roomOpenTimeMillis, nowMillis),
                    accent = MoltenOrange,
                    modifier = Modifier.weight(1f)
                )
                CountdownTile(
                    label = "MATCH STARTS IN",
                    value = formatCountdown(tournament.matchTimeMillis, nowMillis),
                    accent = NeonCrimson,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = ArenaOutline)

            if (!isApproved) {
                Text(
                    text = when (registration?.statusEnum) {
                        RegistrationStatus.PENDING -> "Your registration is pending Admin approval. Room ID and Password will unlock automatically once approved."
                        RegistrationStatus.REJECTED -> "Your registration was rejected. Room credentials are only visible to approved participants."
                        null -> "Register and get approved by Admin to unlock the private Custom Room ID and Password."
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            } else if (roomAccess == null) {
                Text(
                    text = "You are approved! Admin will publish the Room ID & Password here shortly before room open time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MoltenOrange
                )
            } else {
                CredentialCopyRow(
                    label = "ROOM ID",
                    value = roomAccess.roomId,
                    tag = "copy_room_id_button",
                    onCopy = { onCopyText("Room ID", roomAccess.roomId) }
                )
                CredentialCopyRow(
                    label = "PASSWORD",
                    value = roomAccess.roomPassword,
                    tag = "copy_room_password_button",
                    onCopy = { onCopyText("Room Password", roomAccess.roomPassword) }
                )
                if (roomAccess.notes.isNotBlank()) {
                    Text(
                        text = "Lobby Note: ${roomAccess.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ArenaCharcoal,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, ArenaOutline)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = JetBrainsMonoFontFamily,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CredentialCopyRow(
    label: String,
    value: String,
    tag: String,
    onCopy: () -> Unit
) {
    Surface(
        color = ArenaCharcoal,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, LiveEmerald.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = TextPrimary
                )
            }
            OutlinedButton(
                onClick = onCopy,
                border = BorderStroke(1.dp, LiveEmerald),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LiveEmerald),
                modifier = Modifier.testTag(tag)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy $label", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy")
            }
        }
    }
}

@Composable
fun ArenaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    leadingIcon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder, color = TextMuted) }) else null,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = MoltenOrange) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCrimson,
            unfocusedBorderColor = ArenaOutline,
            focusedLabelColor = NeonCrimson,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = NeonCrimson,
            focusedContainerColor = ArenaCharcoal,
            unfocusedContainerColor = ArenaCharcoal
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerRegistrationSheet(
    tournament: Tournament,
    publicProfile: PublicPlayer?,
    privateProfile: PrivatePlayer?,
    defaultName: String,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        playerName: String,
        ign: String,
        freeFireUid: String,
        whatsapp: String,
        teamName: String,
        teammateIgns: List<String>,
        teammateUids: List<String>
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val teammatesNeeded = tournament.modeEnum.teammatesRequired

    var playerName by remember {
        mutableStateOf(privateProfile?.playerName ?: publicProfile?.playerName ?: defaultName)
    }
    var ign by remember { mutableStateOf(privateProfile?.ign ?: publicProfile?.ign ?: "") }
    var ffUid by remember { mutableStateOf(privateProfile?.freeFireUid ?: publicProfile?.freeFireUid ?: "") }
    var whatsapp by remember { mutableStateOf(privateProfile?.whatsappNumber ?: "") }
    var teamName by remember { mutableStateOf("") }

    val mateIgns = remember(teammatesNeeded) {
        mutableStateListOf<String>().apply { repeat(teammatesNeeded) { add("") } }
    }
    val mateUids = remember(teammatesNeeded) {
        mutableStateListOf<String>().apply { repeat(teammatesNeeded) { add("") } }
    }
    var validationError by remember { mutableStateOf<String?>(null) }

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
                text = "REGISTER • ${tournament.modeEnum.label.uppercase()}",
                style = MaterialTheme.typography.headlineMedium,
                color = NeonCrimson
            )
            Text(
                text = "${tournament.title} • Entry: FREE",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            ArenaTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = "Full Player Name *",
                modifier = Modifier.testTag("reg_player_name_input")
            )
            ArenaTextField(
                value = ign,
                onValueChange = { ign = it },
                label = "Free Fire In-Game Name (IGN) *",
                modifier = Modifier.testTag("reg_ign_input")
            )
            ArenaTextField(
                value = ffUid,
                onValueChange = { ffUid = it },
                label = "Free Fire UID *",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.testTag("reg_ff_uid_input")
            )
            ArenaTextField(
                value = whatsapp,
                onValueChange = { whatsapp = it },
                label = "WhatsApp Number (for Admin alerts) *",
                placeholder = "+91 9876543210",
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.testTag("reg_whatsapp_input")
            )

            if (tournament.modeEnum != TournamentMode.SOLO) {
                ArenaTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = "Team / Squad Name *",
                    modifier = Modifier.testTag("reg_team_name_input")
                )

                Text(
                    text = "TEAMMATE ROSTER ($teammatesNeeded required for ${tournament.modeEnum.label})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MoltenOrange
                )
                for (i in 0 until teammatesNeeded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ArenaTextField(
                            value = mateIgns[i],
                            onValueChange = { mateIgns[i] = it },
                            label = "Teammate ${i + 2} IGN *",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_mate_ign_$i")
                        )
                        ArenaTextField(
                            value = mateUids[i],
                            onValueChange = { mateUids[i] = it },
                            label = "UID *",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_mate_uid_$i")
                        )
                    }
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed
                )
            }

            Button(
                onClick = {
                    val err = when {
                        playerName.trim().length < 2 -> "Enter your full player name."
                        ign.trim().length < 2 -> "Enter your Free Fire IGN."
                        ffUid.trim().length < 4 -> "Enter a valid Free Fire UID (4+ digits)."
                        whatsapp.trim().length < 6 -> "Enter a valid WhatsApp number."
                        tournament.modeEnum != TournamentMode.SOLO && teamName.trim().length < 2 ->
                            "Enter your Team Name."
                        (0 until teammatesNeeded).any { mateIgns[it].trim().length < 2 || mateUids[it].trim().length < 4 } ->
                            "Please enter IGN and UID for all $teammatesNeeded teammates."
                        else -> null
                    }
                    if (err != null) {
                        validationError = err
                    } else {
                        validationError = null
                        onSubmit(
                            playerName.trim(),
                            ign.trim(),
                            ffUid.trim(),
                            whatsapp.trim(),
                            if (tournament.modeEnum == TournamentMode.SOLO) ign.trim() else teamName.trim(),
                            mateIgns.map { it.trim() },
                            mateUids.map { it.trim() }
                        )
                    }
                },
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("reg_submit_button")
            ) {
                if (isBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = TextPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Confirm Free Registration", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun RegistrationConfirmationDialog(
    tournamentTitle: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ArenaSurface,
        icon = {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = LiveEmerald, modifier = Modifier.size(44.dp))
        },
        title = {
            Text("Registration Confirmed!", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
        },
        text = {
            Text(
                text = "Your entry for $tournamentTitle has been submitted. Once Admin approves your slot, the Custom Room ID & Password will unlock inside the tournament page.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                modifier = Modifier.testTag("reg_confirm_ok_button")
            ) {
                Text("Got It")
            }
        }
    )
}

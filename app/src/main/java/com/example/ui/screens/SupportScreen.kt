package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.remote.PRIMARY_ADMIN_EMAIL
import com.example.ui.components.EsportsCard
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.ArenaCharcoal
import com.example.ui.theme.ArenaOutline
import com.example.ui.theme.LiveEmerald
import com.example.ui.theme.MoltenOrange
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onToastMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = ArenaBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ArenaCharcoal, titleContentColor = TextPrimary),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("support_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                title = { Text("SUPPORT, RULES & LEGAL", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contact Admin & WhatsApp Support
            item {
                EsportsCard(highlightColor = NeonCrimson.copy(alpha = 0.55f)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = NeonCrimson)
                            Text("CONTACT ADMIN & SUPPORT", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        Text(
                            text = "Need help with slot verification, room credentials, or match disputes? Reach the FF Arena tournament desk directly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$PRIMARY_ADMIN_EMAIL")
                                        putExtra(Intent.EXTRA_SUBJECT, "FF Arena Tournament Support")
                                    }
                                    runCatching { context.startActivity(emailIntent) }
                                        .onFailure { onToastMessage("Email Admin at: $PRIMARY_ADMIN_EMAIL") }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("contact_admin_email_btn")
                            ) {
                                Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Contact Admin")
                            }
                            Button(
                                onClick = {
                                    val msg = Uri.encode("Hello FF Arena Admin, I need tournament support.")
                                    val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$msg"))
                                    runCatching { context.startActivity(waIntent) }
                                        .onFailure { onToastMessage("WhatsApp not installed. Contact: $PRIMARY_ADMIN_EMAIL") }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LiveEmerald, contentColor = ArenaBlack),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("whatsapp_support_btn")
                            ) {
                                Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("WhatsApp Support")
                            }
                        }
                    }
                }
            }

            // Official Tournament Rules Page
            item {
                EsportsCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Gavel, contentDescription = null, tint = MoltenOrange)
                            Text("OFFICIAL TOURNAMENT RULES", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        HorizontalDivider(color = ArenaOutline)
                        Text(
                            text = """
                                1. Fair Play & Anti-Cheat: Any use of hacks, third-party scripts, GFX exploits, or teaming with opponents results in immediate disqualification.
                                2. Mobile-Only Lobbies: Unless explicitly marked open-platform, emulators are strictly prohibited.
                                3. Accurate UID & IGN: Register with your exact Free Fire UID and IGN. Unregistered names in custom rooms will be kicked before start.
                                4. Lobby Timing: Room ID & Password unlock in Tournament Details once your registration is approved. Join 10 minutes before match start.
                                5. Standard Battle Royale Points: Booyah (#1) = 12 pts, #2 = 9 pts, #3 = 8 pts, #4 = 7 pts, #5 = 6 pts, #6 = 5 pts, #7 = 4 pts, #8 = 3 pts, #9 = 2 pts, #10 = 1 pt, plus 1 point per elimination.
                                6. Free Entry Policy: All FF Arena tournaments are 100% free to enter with zero cash wagering or gambling.
                            """.trimIndent(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // FAQ Accordion
            item {
                Text("FREQUENTLY ASKED QUESTIONS", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FaqItem(
                        question = "How do I get the Custom Room ID and Password?",
                        answer = "Register for a tournament from the Tournament Details screen. Once the Admin approves your slot, the Room ID & Password card unlocks automatically in Tournament Details with copy buttons."
                    )
                    FaqItem(
                        question = "Are FF Arena tournaments free to join?",
                        answer = "Yes! Every tournament in FF Arena is completely free to enter. There are no entry fees, betting, or wagering."
                    )
                    FaqItem(
                        question = "How are Duo and Squad teammates registered?",
                        answer = "Select a Duo or Squad tournament and tap Register Now. The form dynamically asks for your Team Name plus your teammates' Free Fire IGNs and UIDs."
                    )
                    FaqItem(
                        question = "When do the leaderboard and profile stats update?",
                        answer = "As soon as the Admin posts match kills and placement points in the Admin Panel, the Leaderboard and your Career Profile stats update automatically in real time."
                    )
                }
            }

            // About Us
            item {
                EsportsCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = MoltenOrange)
                            Text("ABOUT FF ARENA", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        }
                        Text(
                            text = "FF Arena is a dedicated Free Fire esports tournament management platform built for organized Solo, Duo, and Squad competitive scrims across Battle Royale and Clash Squad modes. Our mission is to provide transparent brackets, secure custom room distribution, and verified real-time leaderboards.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Privacy Policy & Terms
            item {
                ExpandableLegalCard(
                    icon = Icons.Filled.PrivacyTip,
                    title = "PRIVACY POLICY",
                    body = "FF Arena authenticates players using Google Sign-In via Android Credential Manager. Your email address and WhatsApp contact number are stored in a restricted private player collection accessible strictly to your account and verified tournament administrators for match coordination. Public leaderboards display only your In-Game Name (IGN), Free Fire UID, and match statistics—never your private phone number or email."
                )
            }
            item {
                ExpandableLegalCard(
                    icon = Icons.Filled.Description,
                    title = "TERMS AND CONDITIONS",
                    body = "By participating in FF Arena tournaments, you agree to abide by fair-play esports rules and respect tournament administrators' final match decisions. FF Arena is an independent community esports organizer and is not affiliated with Garena International. This platform strictly prohibits real-money betting, gambling, or cash-wagering of any kind."
                )
            }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    EsportsCard(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(question, style = MaterialTheme.typography.titleSmall, color = TextPrimary, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MoltenOrange
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpandableLegalCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    var expanded by remember { mutableStateOf(false) }
    EsportsCard(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(icon, contentDescription = null, tint = MoltenOrange)
                    Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

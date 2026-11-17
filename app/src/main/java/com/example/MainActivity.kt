package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.remote.ArenaRepository
import com.example.data.remote.attemptAutoSignIn
import com.example.data.remote.authStateFlow
import com.example.ui.components.FFArenaTopBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TournamentDetailScreen
import com.example.ui.screens.TournamentsScreen
import com.example.ui.state.ArenaViewModel
import com.example.ui.state.UiState
import com.example.ui.theme.ArenaBlack
import com.example.ui.theme.ArenaCharcoal
import com.example.ui.theme.ArenaSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.FFArenaTheme
import com.example.ui.theme.LiveEmerald
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCrimsonSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

object ArenaRoutes {
    const val HOME = "home"
    const val TOURNAMENTS = "tournaments"
    const val LEADERBOARD = "leaderboard"
    const val PROFILE = "profile"
    const val PROFILE_EDIT = "profile?edit={edit}"
    const val TOURNAMENT_DETAIL = "tournament_detail/{tournamentId}"
    const val NOTIFICATIONS = "notifications?tab={tab}"
    const val SUPPORT = "support"
    const val ADMIN = "admin"

    fun detail(tournamentId: String) = "tournament_detail/$tournamentId"
    fun notifications(tab: Int = 0) = "notifications?tab=$tab"
    fun profile(edit: Boolean = false) = "profile?edit=$edit"
}

private data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(ArenaBlack.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(ArenaBlack.toArgb())
        )
        setContent {
            FFArenaTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var autoSignInChecked by remember { mutableStateOf(auth.currentUser != null) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            val credentialManager = CredentialManager.create(context)
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { autoSignInChecked = true },
                onUnauthenticated = { autoSignInChecked = true },
                scope = scope
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = { /* AuthStateListener drives transition */ })
    } else {
        key(user.uid) {
            AuthenticatedArenaApp(user = user)
        }
    }
}

@Composable
private fun AuthenticatedArenaApp(user: FirebaseUser) {
    val viewModel: ArenaViewModel = viewModel(
        key = user.uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as Application
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ArenaViewModel(
                    repository = ArenaRepository(db),
                    currentUserId = user.uid,
                    currentUserEmail = user.email.orEmpty(),
                    isEmailVerified = user.isEmailVerified,
                    googleDisplayName = user.displayName.orEmpty(),
                    googlePhotoUrl = user.photoUrl?.toString()
                )
            }
        }
    )

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val tournamentsState by viewModel.tournamentsState.collectAsStateWithLifecycle()
    val playersState by viewModel.publicPlayersState.collectAsStateWithLifecycle()
    val leaderboardState by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val myPublicProfile by viewModel.myPublicProfile.collectAsStateWithLifecycle()
    val myPrivateProfile by viewModel.myPrivateProfile.collectAsStateWithLifecycle()
    val myRegistrations by viewModel.myRegistrations.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val allRegistrations by viewModel.allRegistrationsForAdmin.collectAsStateWithLifecycle()
    val adminsList by viewModel.adminsList.collectAsStateWithLifecycle()
    val selectedRoomAccess by viewModel.selectedRoomAccess.collectAsStateWithLifecycle()
    val nowMillis by viewModel.currentTimeMillis.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val feedback by viewModel.feedback.collectAsStateWithLifecycle()

    LaunchedEffect(feedback) {
        val current = feedback
        if (current != null) {
            snackbarHostState.showSnackbar(current.message)
            viewModel.clearFeedback()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ArenaRoutes.HOME

    val bottomTabs = remember {
        listOf(
            BottomTab(ArenaRoutes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            BottomTab(ArenaRoutes.TOURNAMENTS, "Events", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "nav_tournaments"),
            BottomTab(ArenaRoutes.LEADERBOARD, "Rankings", Icons.Filled.Leaderboard, Icons.Outlined.Leaderboard, "nav_leaderboard"),
            BottomTab(ArenaRoutes.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
        )
    }

    val isTopLevelRoute = currentRoute == ArenaRoutes.HOME ||
        currentRoute == ArenaRoutes.TOURNAMENTS ||
        currentRoute == ArenaRoutes.LEADERBOARD ||
        currentRoute.startsWith(ArenaRoutes.PROFILE)

    val scope = rememberCoroutineScope()
    val showSnackbar: (String) -> Unit = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaBlack)
    ) {
        val useRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail && isTopLevelRoute) {
                NavigationRail(
                    containerColor = ArenaCharcoal,
                    contentColor = TextPrimary
                ) {
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute == tab.route ||
                            (tab.route == ArenaRoutes.PROFILE && currentRoute.startsWith(ArenaRoutes.PROFILE))
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(ArenaRoutes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = NeonCrimson,
                                selectedTextColor = NeonCrimson,
                                indicatorColor = NeonCrimsonSoft,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            }

            Scaffold(
                containerColor = ArenaBlack,
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = ArenaSurface,
                            contentColor = TextPrimary,
                            actionColor = LiveEmerald
                        )
                    }
                },
                topBar = {
                    if (isTopLevelRoute) {
                        val headerTitle = when {
                            currentRoute == ArenaRoutes.HOME -> "FF ARENA"
                            currentRoute == ArenaRoutes.TOURNAMENTS -> "TOURNAMENTS"
                            currentRoute == ArenaRoutes.LEADERBOARD -> "LEADERBOARD"
                            else -> "PLAYER PROFILE"
                        }
                        FFArenaTopBar(
                            title = headerTitle,
                            unreadCount = unreadCount,
                            isAdmin = isAdmin,
                            onNotificationsClick = { navController.navigate(ArenaRoutes.notifications(0)) },
                            onSupportClick = { navController.navigate(ArenaRoutes.SUPPORT) },
                            onAdminClick = { navController.navigate(ArenaRoutes.ADMIN) }
                        )
                    }
                },
                bottomBar = {
                    if (!useRail && isTopLevelRoute) {
                        NavigationBar(
                            containerColor = ArenaCharcoal,
                            contentColor = TextPrimary
                        ) {
                            bottomTabs.forEach { tab ->
                                val selected = currentRoute == tab.route ||
                                    (tab.route == ArenaRoutes.PROFILE && currentRoute.startsWith(ArenaRoutes.PROFILE))
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(ArenaRoutes.HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NeonCrimson,
                                        selectedTextColor = NeonCrimson,
                                        indicatorColor = NeonCrimsonSoft,
                                        unselectedIconColor = TextMuted,
                                        unselectedTextColor = TextMuted
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(modifier = Modifier.widthIn(max = 840.dp).fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = ArenaRoutes.HOME
                        ) {
                            composable(ArenaRoutes.HOME) {
                                HomeScreen(
                                    tournamentsState = tournamentsState,
                                    playersState = playersState,
                                    myProfile = myPublicProfile,
                                    displayName = viewModel.googleDisplayName,
                                    myRegistrations = myRegistrations,
                                    announcements = announcements,
                                    nowMillis = nowMillis,
                                    isAdmin = isAdmin,
                                    onOpenTournament = { tid -> navController.navigate(ArenaRoutes.detail(tid)) },
                                    onBrowseAllTournaments = {
                                        navController.navigate(ArenaRoutes.TOURNAMENTS) {
                                            popUpTo(ArenaRoutes.HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onOpenAdmin = { navController.navigate(ArenaRoutes.ADMIN) },
                                    onSetupProfile = { navController.navigate(ArenaRoutes.profile(edit = true)) },
                                    onOpenAnnouncements = { navController.navigate(ArenaRoutes.notifications(1)) }
                                )
                            }

                            composable(ArenaRoutes.TOURNAMENTS) {
                                TournamentsScreen(
                                    tournamentsState = tournamentsState,
                                    myRegistrations = myRegistrations,
                                    nowMillis = nowMillis,
                                    onOpenTournament = { tid -> navController.navigate(ArenaRoutes.detail(tid)) }
                                )
                            }

                            composable(ArenaRoutes.LEADERBOARD) {
                                LeaderboardScreen(
                                    leaderboardState = leaderboardState,
                                    playersState = playersState,
                                    tournamentsState = tournamentsState
                                )
                            }

                            composable(ArenaRoutes.PROFILE) {
                                val allLeaderboard = (leaderboardState as? UiState.Success)?.data ?: emptyList()
                                val myHistory = allLeaderboard.filter { it.userId == viewModel.currentUserId }
                                ProfileScreen(
                                    currentUserId = viewModel.currentUserId,
                                    currentUserEmail = viewModel.currentUserEmail,
                                    googleDisplayName = viewModel.googleDisplayName,
                                    googlePhotoUrl = viewModel.googlePhotoUrl,
                                    publicProfile = myPublicProfile,
                                    privateProfile = myPrivateProfile,
                                    myRegistrations = myRegistrations,
                                    myMatchHistory = myHistory,
                                    isAdmin = isAdmin,
                                    isBusy = isBusy,
                                    openEditorInitially = false,
                                    onSaveProfile = viewModel::saveProfile,
                                    onOpenTournament = { tid -> navController.navigate(ArenaRoutes.detail(tid)) },
                                    onOpenAdmin = { navController.navigate(ArenaRoutes.ADMIN) },
                                    onOpenSupport = { navController.navigate(ArenaRoutes.SUPPORT) },
                                    onSignedOut = {}
                                )
                            }

                            composable(
                                route = ArenaRoutes.PROFILE_EDIT,
                                arguments = listOf(navArgument("edit") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                })
                            ) { backStackEntry ->
                                val editFlag = backStackEntry.arguments?.getBoolean("edit") ?: false
                                val allLeaderboard = (leaderboardState as? UiState.Success)?.data ?: emptyList()
                                val myHistory = allLeaderboard.filter { it.userId == viewModel.currentUserId }
                                ProfileScreen(
                                    currentUserId = viewModel.currentUserId,
                                    currentUserEmail = viewModel.currentUserEmail,
                                    googleDisplayName = viewModel.googleDisplayName,
                                    googlePhotoUrl = viewModel.googlePhotoUrl,
                                    publicProfile = myPublicProfile,
                                    privateProfile = myPrivateProfile,
                                    myRegistrations = myRegistrations,
                                    myMatchHistory = myHistory,
                                    isAdmin = isAdmin,
                                    isBusy = isBusy,
                                    openEditorInitially = editFlag,
                                    onSaveProfile = viewModel::saveProfile,
                                    onOpenTournament = { tid -> navController.navigate(ArenaRoutes.detail(tid)) },
                                    onOpenAdmin = { navController.navigate(ArenaRoutes.ADMIN) },
                                    onOpenSupport = { navController.navigate(ArenaRoutes.SUPPORT) },
                                    onSignedOut = {}
                                )
                            }

                            composable(
                                route = ArenaRoutes.TOURNAMENT_DETAIL,
                                arguments = listOf(navArgument("tournamentId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val tid = backStackEntry.arguments?.getString("tournamentId").orEmpty()
                                val tournaments = (tournamentsState as? UiState.Success)?.data ?: emptyList()
                                val tournament = tournaments.firstOrNull { it.id == tid }
                                val reg = myRegistrations.firstOrNull { it.tournamentId == tid }
                                val allLeaderboard = (leaderboardState as? UiState.Success)?.data ?: emptyList()
                                val tLeaderboard = allLeaderboard.filter { it.tournamentId == tid }

                                TournamentDetailScreen(
                                    tournament = tournament,
                                    myRegistration = reg,
                                    publicProfile = myPublicProfile,
                                    privateProfile = myPrivateProfile,
                                    googleDisplayName = viewModel.googleDisplayName,
                                    isAdmin = isAdmin,
                                    roomAccess = selectedRoomAccess,
                                    tournamentLeaderboard = tLeaderboard,
                                    nowMillis = nowMillis,
                                    isBusy = isBusy,
                                    onSelectForRoom = viewModel::selectTournamentForRoom,
                                    onSubmitRegistration = viewModel::registerForTournament,
                                    onCopiedMessage = showSnackbar,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = ArenaRoutes.NOTIFICATIONS,
                                arguments = listOf(navArgument("tab") {
                                    type = NavType.IntType
                                    defaultValue = 0
                                })
                            ) { backStackEntry ->
                                val tab = backStackEntry.arguments?.getInt("tab") ?: 0
                                NotificationsScreen(
                                    notifications = notifications,
                                    announcements = announcements,
                                    initialTab = tab,
                                    onMarkRead = viewModel::markNotificationRead,
                                    onMarkAllRead = viewModel::markAllNotificationsRead,
                                    onOpenTournament = { tid -> navController.navigate(ArenaRoutes.detail(tid)) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(ArenaRoutes.SUPPORT) {
                                SupportScreen(
                                    onToastMessage = showSnackbar,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(ArenaRoutes.ADMIN) {
                                AdminDashboardScreen(
                                    isAdmin = isAdmin,
                                    currentUserId = viewModel.currentUserId,
                                    currentUserEmail = viewModel.currentUserEmail,
                                    tournamentsState = tournamentsState,
                                    allRegistrations = allRegistrations,
                                    leaderboardState = leaderboardState,
                                    adminsList = adminsList,
                                    selectedRoomAccess = selectedRoomAccess,
                                    isBusy = isBusy,
                                    onSelectTournamentForRoom = viewModel::selectTournamentForRoom,
                                    onCreateTournament = viewModel::createTournament,
                                    onUpdateTournament = viewModel::updateTournament,
                                    onDeleteTournament = viewModel::deleteTournament,
                                    onUpdateRegistrationStatus = viewModel::updateRegistrationStatus,
                                    onSaveRoomAccess = viewModel::saveRoomAccess,
                                    onUpsertLeaderboard = viewModel::upsertLeaderboardEntry,
                                    onDeleteLeaderboardEntry = viewModel::deleteLeaderboardEntry,
                                    onAnnounceWinners = viewModel::announceWinners,
                                    onSendAnnouncement = viewModel::sendAnnouncement,
                                    onSendDirectNotification = viewModel::sendDirectNotification,
                                    onGrantAdmin = viewModel::grantAdminAccess,
                                    onRevokeAdmin = viewModel::revokeAdminAccess,
                                    onToastMessage = showSnackbar,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

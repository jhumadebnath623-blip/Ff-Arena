package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ColorOutlineSubtle = androidx.compose.ui.graphics.Color(0xFF232736)

private val FFArenaDarkColorScheme = darkColorScheme(
    primary = NeonCrimson,
    onPrimary = TextPrimary,
    primaryContainer = NeonCrimsonDark,
    onPrimaryContainer = TextPrimary,
    secondary = MoltenOrange,
    onSecondary = ArenaBlack,
    secondaryContainer = MoltenOrangeSoft,
    onSecondaryContainer = MoltenOrangeBright,
    tertiary = ChampionshipGold,
    onTertiary = ArenaBlack,
    background = ArenaBlack,
    onBackground = TextPrimary,
    surface = ArenaCharcoal,
    onSurface = TextPrimary,
    surfaceVariant = ArenaSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ArenaBlack,
    surfaceContainerLow = ArenaCharcoal,
    surfaceContainer = ArenaSurface,
    surfaceContainerHigh = ArenaSurfaceElevated,
    outline = ArenaOutline,
    outlineVariant = ColorOutlineSubtle,
    error = ErrorRed,
    onError = TextPrimary
)

val FFArenaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun FFArenaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FFArenaDarkColorScheme,
        typography = FFArenaTypography,
        shapes = FFArenaShapes,
        content = content
    )
}

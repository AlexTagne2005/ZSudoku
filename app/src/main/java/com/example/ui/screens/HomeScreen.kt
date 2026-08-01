package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.StatsEntity
import com.example.model.Difficulty

@Composable
fun HomeScreen(
    hasSavedGame: Boolean,
    savedDifficulty: String,
    savedTime: Long,
    stats: StatsEntity,
    onNewGameSelect: (Difficulty) -> Unit,
    onStartDailyChallenge: () -> Unit,
    onResumeGameSelect: () -> Unit,
    onOpenStatsSelect: () -> Unit,
    onOpenSettingsSelect: () -> Unit,
    onOpenTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var lockedDialogInfo by remember { mutableStateOf<LockedDialogData?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Hero Card with Generated Zen Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_zen_banner_1785002626902),
                        contentDescription = "Zen Garden Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.35f)
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ZenSudoku",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            text = "Calm, frictionless Sudoku",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Challenge Card
            val todayStr = remember {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            }
            val todayDisplayStr = remember {
                java.text.SimpleDateFormat("EEEE d MMMM", java.util.Locale.FRENCH).format(java.util.Date()).replaceFirstChar { it.uppercase() }
            }
            val isDailyDone = stats.lastDailyCompletedDate == todayStr

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("daily_challenge_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "DÉFI DU JOUR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        if (isDailyDone) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "⭐ Réussi !",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = todayDisplayStr,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = if (isDailyDone) "Vous avez complété la grille unique d'aujourd'hui !" else "Une grille unique générée pour tous les joueurs aujourd'hui.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    Button(
                        onClick = onStartDailyChallenge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_daily_challenge_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDailyDone) "Rejouer le Défi Quotidien" else "Relever le Défi Quotidien",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Resume Game Button if available
            if (hasSavedGame) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { onResumeGameSelect() }
                        .testTag("resume_game_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = "💾 BROUILLON SAUVEGARDÉ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "Reprendre la partie",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Niveau: $savedDifficulty • Temps écoulé: ${formatTime(savedTime)}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Difficulty Selection Header
            Text(
                text = "Choisir une difficulté",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            DifficultyGrid(
                stats = stats,
                onDifficultySelect = onNewGameSelect,
                onLockedDifficultyClick = { targetDiff, reqPrevDiff, curStars ->
                    lockedDialogInfo = LockedDialogData(targetDiff, reqPrevDiff, curStars)
                }
            )
        }

        // Persistent Fixed Bottom Navigation Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Default.HelpOutline,
                    label = "Guide",
                    onClick = onOpenTutorial,
                    testTag = "bottom_nav_guide"
                )
                BottomNavItem(
                    icon = Icons.Default.AccountCircle,
                    label = "Profil",
                    onClick = onOpenStatsSelect,
                    testTag = "bottom_nav_profile"
                )
                BottomNavItem(
                    icon = Icons.Default.BarChart,
                    label = "Stats",
                    onClick = onOpenStatsSelect,
                    testTag = "bottom_nav_stats"
                )
                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = "Options",
                    onClick = onOpenSettingsSelect,
                    testTag = "bottom_nav_settings"
                )
            }
        }
    }

    // Modal when user taps a locked level
    lockedDialogInfo?.let { data ->
        AlertDialog(
            onDismissRequest = { lockedDialogInfo = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Niveau Verrouillé 🔒",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Pour débloquer le niveau ${data.targetDifficulty.displayName}, vous devez obtenir au moins 3 étoiles en réussissant des grilles au niveau ${data.requiredPrevDifficulty.displayName}.",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Étoiles ${data.requiredPrevDifficulty.displayName}:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "⭐ ${data.currentStars} / 3 requis",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { lockedDialogInfo = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Compris", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private data class LockedDialogData(
    val targetDifficulty: Difficulty,
    val requiredPrevDifficulty: Difficulty,
    val currentStars: Int
)

@Composable
private fun DifficultyGrid(
    stats: StatsEntity,
    onDifficultySelect: (Difficulty) -> Unit,
    onLockedDifficultyClick: (target: Difficulty, reqPrev: Difficulty, currentStars: Int) -> Unit
) {
    // Hierarchical lock criteria:
    // EASY: Unlocked by default
    // MEDIUM: Unlocked if easyStars >= 3
    // HARD: Unlocked if mediumStars >= 3
    // EXPERT: Unlocked if hardStars >= 3

    val levelInfos = listOf(
        LevelItem(
            difficulty = Difficulty.EASY,
            description = "Accessible pour débuter sereinement",
            starsObtained = stats.easyStars,
            isUnlocked = true,
            requiredPrevDiff = Difficulty.EASY,
            requiredStars = 0,
            prevStarsCurrent = 0
        ),
        LevelItem(
            difficulty = Difficulty.MEDIUM,
            description = "Réfléchi & équilibré",
            starsObtained = stats.mediumStars,
            isUnlocked = stats.easyStars >= 3,
            requiredPrevDiff = Difficulty.EASY,
            requiredStars = 3,
            prevStarsCurrent = stats.easyStars
        ),
        LevelItem(
            difficulty = Difficulty.HARD,
            description = "Défie votre logique profonde",
            starsObtained = stats.hardStars,
            isUnlocked = stats.mediumStars >= 3,
            requiredPrevDiff = Difficulty.MEDIUM,
            requiredStars = 3,
            prevStarsCurrent = stats.mediumStars
        ),
        LevelItem(
            difficulty = Difficulty.EXPERT,
            description = "Maîtrise & concentration zen",
            starsObtained = stats.expertStars,
            isUnlocked = stats.hardStars >= 3,
            requiredPrevDiff = Difficulty.HARD,
            requiredStars = 3,
            prevStarsCurrent = stats.hardStars
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        levelInfos.forEach { item ->
            Surface(
                onClick = {
                    if (item.isUnlocked) {
                        onDifficultySelect(item.difficulty)
                    } else {
                        onLockedDifficultyClick(item.difficulty, item.requiredPrevDiff, item.prevStarsCurrent)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                color = if (item.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = if (item.isUnlocked) 2.dp else 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("difficulty_${item.difficulty.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.difficulty.displayName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!item.isUnlocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Verrouillé",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = item.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        // Display stars obtained
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${item.starsObtained} Étoiles accumulées",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (item.isUnlocked) {
                        Button(
                            onClick = { onDifficultySelect(item.difficulty) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Jouer", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "🔒 3 ⭐ ${item.requiredPrevDiff.displayName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class LevelItem(
    val difficulty: Difficulty,
    val description: String,
    val starsObtained: Int,
    val isUnlocked: Boolean,
    val requiredPrevDiff: Difficulty,
    val requiredStars: Int,
    val prevStarsCurrent: Int
)

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


package com.example.model

import com.example.data.StatsEntity

data class BrainTier(
    val level: Int,
    val title: String,
    val iconEmoji: String,
    val description: String,
    val minPoints: Int,
    val maxPoints: Int
)

data class BrainLevelInfo(
    val level: Int,
    val title: String,
    val iconEmoji: String,
    val currentPoints: Int,
    val nextLevelPoints: Int,
    val winRatePercentage: Int,
    val averageTimeSeconds: Long,
    val progressFraction: Float,
    val gamesNeededForNextTier: Int,
    val allTiers: List<BrainTier>
)

object BrainEvolutionCalculator {
    val TIERS = listOf(
        BrainTier(1, "Neurone Intuitif", "🌱", "Eveil de la conscience logique", 0, 250),
        BrainTier(2, "Réseau Synaptique", "💡", "Connexions rapides et schémas", 250, 600),
        BrainTier(3, "Cortex Rapide", "⚡", "Résolution fluide et analytique", 600, 1200),
        BrainTier(4, "Conscience Quantique", "🔮", "Maîtrise spatiale et précision", 1200, 2200),
        BrainTier(5, "Esprit Zen Absolu", "🌌", "Eveil total et harmonie numérique", 2200, 5000)
    )

    fun calculate(stats: StatsEntity): BrainLevelInfo {
        val totalCompleted = stats.easyCompleted + stats.mediumCompleted + stats.hardCompleted + stats.expertCompleted
        val totalStars = stats.easyStars + stats.mediumStars + stats.hardStars + stats.expertStars
        val winRate = if (stats.totalGamesPlayed > 0) {
            ((totalCompleted.toFloat() / stats.totalGamesPlayed.toFloat()) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }

        // Calculate cognitive score
        val pointsFromCompleted = totalCompleted * 120
        val pointsFromStars = totalStars * 45
        val pointsFromStreak = stats.bestStreak * 150
        val pointsFromDaily = stats.dailyCompletedCount * 100

        val totalPoints = pointsFromCompleted + pointsFromStars + pointsFromStreak + pointsFromDaily

        val activeTier = TIERS.lastOrNull { totalPoints >= it.minPoints } ?: TIERS.first()
        val nextTier = TIERS.firstOrNull { it.minPoints > activeTier.minPoints } ?: activeTier

        val minPts = activeTier.minPoints
        val maxPts = activeTier.maxPoints
        val fraction = ((totalPoints - minPts).toFloat() / (maxPts - minPts).toFloat()).coerceIn(0f, 1f)

        val remainingPts = (maxPts - totalPoints).coerceAtLeast(0)
        val gamesNeeded = if (activeTier.level == 5) 0 else ((remainingPts + 159) / 160).coerceAtLeast(1)

        val times = listOf(stats.bestTimeEasy, stats.bestTimeMedium, stats.bestTimeHard, stats.bestTimeExpert).filter { it > 0 }
        val avgTime = if (times.isNotEmpty()) times.average().toLong() else 0L

        return BrainLevelInfo(
            level = activeTier.level,
            title = activeTier.title,
            iconEmoji = activeTier.iconEmoji,
            currentPoints = totalPoints,
            nextLevelPoints = maxPts,
            winRatePercentage = winRate,
            averageTimeSeconds = avgTime,
            progressFraction = fraction,
            gamesNeededForNextTier = gamesNeeded,
            allTiers = TIERS
        )
    }
}

data class ZenBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val category: String,
    val isUnlocked: Boolean,
    val progressText: String
)

object BadgeManager {
    fun getBadges(stats: StatsEntity): List<ZenBadge> {
        val totalCompleted = stats.easyCompleted + stats.mediumCompleted + stats.hardCompleted + stats.expertCompleted
        val totalStars = stats.easyStars + stats.mediumStars + stats.hardStars + stats.expertStars

        return listOf(
            ZenBadge(
                id = "first_win",
                title = "Initié Zen",
                description = "Résoudre votre toute première grille",
                iconEmoji = "🧘",
                category = "Grilles",
                isUnlocked = totalCompleted >= 1,
                progressText = "$totalCompleted / 1"
            ),
            ZenBadge(
                id = "speed_demon",
                title = "Esprit Éclair",
                description = "Résoudre une grille en moins de 3 minutes",
                iconEmoji = "⚡",
                category = "Vitesse",
                isUnlocked = stats.bestTimeEasy in 1..180 || stats.bestTimeMedium in 1..180,
                progressText = if (stats.bestTimeEasy > 0) "${stats.bestTimeEasy}s" else "--"
            ),
            ZenBadge(
                id = "streak_master",
                title = "Flamme Intemporelle",
                description = "Atteindre une série de 3 victoires consécutives",
                iconEmoji = "🔥",
                category = "Série",
                isUnlocked = stats.bestStreak >= 3,
                progressText = "${stats.bestStreak} / 3"
            ),
            ZenBadge(
                id = "star_collector",
                title = "Constellation Zen",
                description = "Récolter au moins 15 étoiles de complétion",
                iconEmoji = "⭐",
                category = "Étoiles",
                isUnlocked = totalStars >= 15,
                progressText = "$totalStars / 15"
            ),
            ZenBadge(
                id = "expert_solver",
                title = "Maître des Chiffres",
                description = "Résoudre au moins 10 grilles au total",
                iconEmoji = "🏆",
                category = "Maîtrise",
                isUnlocked = totalCompleted >= 10,
                progressText = "$totalCompleted / 10"
            ),
            ZenBadge(
                id = "zen_botanist",
                title = "Jardinier Céleste",
                description = "Résoudre au moins 3 Défis Quotidiens",
                iconEmoji = "🌸",
                category = "Arbre Zen",
                isUnlocked = stats.dailyCompletedCount >= 3,
                progressText = "${stats.dailyCompletedCount} / 3"
            )
        )
    }
}

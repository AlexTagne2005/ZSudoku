# 🧩 ZenSudoku — Application Android Native

**ZenSudoku** est une application Android moderne, élégante et apaisante développée en **Kotlin** et **Jetpack Compose**. Conçue pour offrir une expérience de jeu fluide, gratifiante et sans stress, l'application associe un générateur de grille de Sudoku performant avec un système unique de **Maturation Cérébrale** (Brain Evolution).

---

## 🌟 Fonctionnalités Clés

### 🧠 Évolution Cérébrale & Arbre Synaptique
- **5 Paliers de Progression** : Du *Neurone Intuitif* à l'*Esprit Zen Absolu*.
- **Calcul Dynamique du Score Cognitif** : Calculé en fonction du taux de réussite, des étoiles obtenues, des séries de victoires et du temps moyen de résolution.
- **Carte Résumé & Indicateur de Progression** : Visualisez le nombre de victoires au rythme actuel nécessaires pour atteindre le niveau supérieur.
- **Animation Célebratoire d'Élévation** : Fenêtre modale animée déclenchée lors de la remise d'un nouveau palier.

### 🎮 Gameplay Sudoku & Modes Intelligents
- **4 Niveaux de Difficulté** : Facile, Moyen, Difficile, Expert.
- **Mode Adaptatif Intelligent** : Ajuste la difficulté en temps réel selon la vitesse et la précision du joueur.
- **Défi Quotidien (Daily Challenge)** : Un casse-tête unique chaque jour avec calendrier de suivi et récompenses d'étoiles.
- **Saisie Flexible** :
  - *Cellule d'abord* ou *Chiffre d'abord*.
  - Mode *Crayon / Notes* automatique ou manuel.
  - Annulation & Rétablissement illimités (*Undo / Redo*).
- **Gestes & Secousse (Shake-to-Erase)** : Secouez le téléphone ou effectuez des glissements rapides pour effacer ou basculer les notes (avec module d'entraînement aux gestes).

### 🎶 Ambiance Sonore Synthétisée
- Générateur audio ambiant temps réel utilisant `AudioTrack` pour produire des fréquences douces binaurales et méditatives pendant vos sessions de réflexion.

---

## 🛠️ Architecture & Technologies

- **UI Framework** : [Jetpack Compose](https://developer.android.com/jetpack/compose) avec Design System [Material 3](https://m3.material.io/)
- **Langage** : Kotlin 100% Native
- **Persistance des Données** : [Room Database](https://developer.android.com/training/data-storage/room) & KSP (Caches de grilles, statistiques, paramètres utilisateur)
- **Gestion d'État** : ViewModel, `StateFlow` & `collectAsStateWithLifecycle`
- **Navigation** : Jetpack Navigation Compose
- **Thèmes & Accessibilité** : Support automatique des thèmes Clair / Sombre, animations fluides et cibles tactiles conformes aux normes M3.

---

## 🚀 Compilateur & Lancement

```bash
# Compilation et vérification du projet
./gradlew assembleDebug

# Exécution des tests unitaires
./gradlew testDebugUnitTest
```

---

## 📄 Licence

Projet développé avec passion pour l'entraînement cognitif et la sérénité.


# 2048 — COMP2042 Coursework Resit

A fully refactored and extended JavaFX implementation of the classic 2048 sliding-tile puzzle.

---

## GitHub Repository

>https://github.com/edwindows11/CW_Resit.git

---

## Compilation Instructions

### Prerequisites
| Tool | Version |
|------|---------|
| JDK  | 21 (Eclipse Adoptium recommended) |
| Maven | bundled via `mvnw.cmd` (no install needed) |

### Run the application
```powershell
# Windows (from the CW_Resit-master directory)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot"
.\mvnw.cmd clean javafx:run
```

### Run tests only
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot"
.\mvnw.cmd test
```

---

## Implemented and Working Properly

| Feature | Description |
|---------|-------------|
| **Bug fixes** | All 9 bugs from the original codebase corrected (see Modified Classes below) |
| **Package reorganisation** | Code split into `core`, `ui`, `controller`, `model`, `util` sub-packages |
| **Single Responsibility** | `GameScene` (500+ line god-class) split into 8 focused classes |
| **Observer pattern** | `Board` notifies `GameView` and `GameController` of state changes |
| **Strategy pattern** | All four move directions share one `compactLine` primitive via `MoveStrategy` lambdas |
| **Factory pattern** | `ColorMapper` maps tile values to colours; `TextMaker` creates styled text nodes |
| **JUnit tests** | `BoardTest`, `ScoreManagerTest`, `UndoManagerTest` (JUnit 5) |
| **Dark mode UI** | Full dark-mode design (`#1a1a2e` background, tile gradient, glowing title) |
| **Level selection** | Main menu offers Easy 3×3, Classic 4×4, Hard 5×5 modes |
| **Undo move** | Up to 3 undos per game via `Ctrl+Z` or the UNDO button |
| **In-memory leaderboard** | Top-10 scores per session, sorted descending; enter your name at game end |
| **Tile animations** | Merge pop (`ScaleTransition`) and spawn fade-in (`FadeTransition` + `ScaleTransition`) |
| **Time Challenge mode** | 60-second countdown; every 15 s a ×2 score-multiplier fires for 5 s with a gold grid glow |

---

## Implemented but Not Working Properly

None at time of submission. All features tested across all grid sizes.

---

## Features Not Implemented

| Feature | Reason |
|---------|--------|
| Persistent leaderboard (file) | Per specification, in-memory storage was chosen |
| Slide animations (tile movement) | Requires architectural change to absolute-position rendering rather than GridPane; excluded in favour of merge/spawn animations which are simpler and equally impactful |

---

## New Java Classes

| Class | Package | Purpose |
|-------|---------|---------|
| `GameState` | `core` | Enum replacing the confusing triple-int return from original `haveEmptyCell()` |
| `BoardObserver` | `core` | Observer interface for Board state-change notifications |
| `Board` | `core` | Pure game logic: grid data, all move operations, tile spawning, win/loss detection |
| `ScoreManager` | `core` | Singleton tracking current and best score (merge-only, fixing original bug) |
| `UndoManager` | `core` | Capped deque of board snapshots; tracks 3 undo tokens per session |
| `GameMode` | `model` | Enum of all four playable modes with display name, grid size, and win tile |
| `LeaderboardEntry` | `model` | Immutable Java record for a single leaderboard row |
| `Leaderboard` | `model` | Singleton maintaining top-10 in-memory leaderboard entries |
| `Account` | `model` | Refactored player profile with `Optional`-based lookup |
| `ColorMapper` | `util` | Utility mapping tile values to dark-mode JavaFX `Color`s |
| `TextMaker` | `util` | Singleton text-node factory (refactored from original) |
| `MenuView` | `ui` | Dark-mode main menu with animated buttons and gradient title |
| `GameView` | `ui` | `BoardObserver` that renders the grid with merge/spawn animations |
| `EndGameView` | `ui` | Win/lose screen with name-entry field for leaderboard submission |
| `LeaderboardView` | `ui` | Styled dark-mode top-10 score table |
| `GameController` | `controller` | Orchestrates Board, GameView, ScoreManager, UndoManager, and the Time Challenge timer |
| `MenuController` | `controller` | Central scene-switching hub; manages all transitions between screens |

---

## Modified Java Classes

| Class | Changes Made | Reason |
|-------|-------------|--------|
| `Main.java` | Removed all UI setup code; now just creates `MenuController` and calls `showMenu()` | Original contained 60+ lines of dead code (unused scenes, Scanner, roots) |
| `Cell.java` | Replaced with deprecated stub | Logic extracted to `Board` (data) and `GameView` (rendering) |
| `GameScene.java` | Replaced with deprecated stub | Violated SRP; split into `Board`, `GameController`, `GameView` |
| `EndGame.java` | Replaced with deprecated stub | Replaced by `EndGameView` with proper navigation |
| `Controller.java` | Replaced with deprecated stub | Was completely empty and unused |
| `TextMaker.java` (root) | Replaced with deprecated stub | Moved and refactored to `com.example.demo.util.TextMaker` |
| `Account.java` (root) | Replaced with deprecated stub | Moved and refactored to `com.example.demo.model.Account` |
| `pom.xml` | Added `maven-surefire-plugin 3.2.5`; changed source/target to Java 21 | Enables JUnit 5 test discovery; Java 21 matches JavaFX 21 dependency |
| `module-info.java` | Added exports/opens for all new packages | Required for JavaFX module reflection and new package visibility |

---

## Bugs Fixed

| # | Original Bug | Location | Fix |
|---|-------------|----------|-----|
| 1 | Tile spawn only covered rows 0..n-2 and cols 0..n-2 due to faulty index tracking | `GameScene.randomFillNumber()` | Rewrote using a flat `List<int[]>` of all empty cells; uniform random selection |
| 2 | Score summed ALL tile values after every keypress | `GameScene.sumCellNumbersToScore()` | `ScoreManager` now only adds the merge delta returned by `compactLine()` |
| 3 | Win condition returned a code (0) but never showed a win screen | `GameScene.haveEmptyCell()` | `GameState` enum + `updateState()` correctly transitions to `WON` |
| 4 | Game-over check only compared down-right neighbours, skipping last row/col | `GameScene.haveSameNumberNearly()` | `canMove()` checks right and down neighbours for every cell in the grid |
| 5 | No win screen existed — game continued forever past 2048 | `Main.java` / `GameScene` | `EndGameView` shown with correct win/loss flag when state transitions |
| 6 | Quit button only cleared children, left stage stuck on end-game scene | `EndGame.endGameShow()` | `MenuController.showMenu()` properly resets the stage scene |
| 7 | `GameScene` had static mutable fields `n` and `LENGTH` causing global state | `GameScene` | `Board` uses final instance fields; no static mutable state |
| 8 | `Scanner`, multiple unused `Group`/`Scene` variables | `Main.java` | All dead code removed |
| 9 | `getTextClass()` was private preventing `Cell.adder()` colour update | `Cell.java` | Entire `Cell` replaced by `Board` (data) + `GameView` (rendering) |

---

## Unexpected Problems

- **Java version mismatch**: `pom.xml` originally specified Java 25 as source/target, but JavaFX 21 requires Java 21+. Changed to 21 to match the LTS in use.
- **`module-info.java` placement**: The file was already correctly placed at `src/main/java/module-info.java` (confirmed by Maven); the IDE listing appeared to show it inside the package folder.

---

## Project Structure (final)

```
src/main/java/
├── module-info.java
└── com/example/demo/
    ├── Main.java
    ├── core/
    │   ├── GameState.java
    │   ├── BoardObserver.java
    │   ├── Board.java
    │   ├── ScoreManager.java
    │   └── UndoManager.java
    ├── model/
    │   ├── GameMode.java
    │   ├── LeaderboardEntry.java
    │   ├── Leaderboard.java
    │   └── Account.java
    ├── util/
    │   ├── ColorMapper.java
    │   └── TextMaker.java
    ├── ui/
    │   ├── MenuView.java
    │   ├── GameView.java
    │   ├── EndGameView.java
    │   └── LeaderboardView.java
    └── controller/
        ├── GameController.java
        └── MenuController.java

src/test/java/com/example/demo/core/
    ├── BoardTest.java
    ├── ScoreManagerTest.java
    └── UndoManagerTest.java
```

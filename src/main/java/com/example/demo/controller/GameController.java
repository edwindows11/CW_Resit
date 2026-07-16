package com.example.demo.controller;

import com.example.demo.core.*;
import com.example.demo.model.GameMode;
import com.example.demo.model.Leaderboard;
import com.example.demo.ui.GameView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Orchestrates a single game session: wires together {@link Board},
 * {@link GameView}, {@link ScoreManager}, and {@link UndoManager},
 * handles keyboard input, manages the Time Challenge countdown, and
 * activates the ×2 score multiplier.
 *
 * <p>This class also acts as a {@link BoardObserver} to intercept score deltas
 * and apply the multiplier before forwarding them to {@link ScoreManager}.</p>
 *
 * <p>Scene transitions are delegated to {@link MenuController}.</p>
 */
public class GameController implements BoardObserver {

    // ── Constants ──────────────────────────────────────────────────────────

    /** Total seconds available in Time Challenge mode. */
    private static final int TIME_LIMIT_SECONDS = 60;

    /** Score multiplier fires every this many seconds in Time Challenge. */
    private static final int MULTIPLIER_INTERVAL = 15;

    /** Duration (in seconds) that the ×2 multiplier stays active. */
    private static final int MULTIPLIER_DURATION = 5;

    // ── Collaborators ──────────────────────────────────────────────────────

    private final Board         board;
    private final GameView      gameView;
    private final ScoreManager  scoreManager;
    private final UndoManager   undoManager;
    private final MenuController menuController;
    private final GameMode      mode;
    private final Scene         gameScene;

    // ── Time-challenge state ───────────────────────────────────────────────

    private Timeline   countdownTimer;
    private Timeline   multiplierScheduler;
    private Timeline   multiplierTimer;
    private int        timeRemaining;
    private boolean    multiplierActive = false;
    private boolean    gameActive       = false;

    // ── Constructor ────────────────────────────────────────────────────────

    /**
     * Sets up the game controller for the given mode.
     *
     * @param mode           the {@link GameMode} to play
     * @param menuController the parent controller handling scene transitions
     */
    public GameController(GameMode mode, MenuController menuController) {
        this.mode           = mode;
        this.menuController = menuController;
        this.board          = new Board(mode.getGridSize(), mode.getWinTile());
        this.gameView       = new GameView(mode);
        this.scoreManager   = ScoreManager.getInstance();
        this.undoManager    = new UndoManager();

        // Wire undo/menu callbacks
        gameView.setOnUndo(this::handleUndo);
        gameView.setOnMenu(menuController::showMenu);

        // Register observers: GameView renders first, then this class updates score
        board.addObserver(gameView);
        board.addObserver(this);

        this.gameScene = new Scene(gameView, 800, 800);
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Returns the {@link Scene} containing the game view, ready to be set on a Stage.
     *
     * @return the game scene
     */
    public Scene createScene() { return gameScene; }

    /**
     * Starts the game: initialises the board, resets score, registers key
     * handlers, and (for Time Challenge) starts the countdown timer.
     */
    public void start() {
        scoreManager.reset();
        undoManager.reset();
        gameActive = true;

        board.initialise();

        // Use addEventFilter so we intercept keys BEFORE any focused button
        // can consume them for focus traversal.
        gameScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);

        // Give the game pane keyboard focus immediately
        Platform.runLater(gameView::requestFocus);

        if (mode.isTimedMode()) {
            startCountdown();
            startMultiplierScheduler();
        }
    }

    /**
     * Stops all running timers, used when returning to the menu mid-game.
     */
    public void stop() {
        gameActive = false;
        stopTimers();
    }

    // ── BoardObserver ──────────────────────────────────────────────────────

    /**
     * Receives score deltas from the board, applies the multiplier if active,
     * updates {@link ScoreManager}, refreshes the view's score labels, and
     * handles the win/lose transitions.
     *
     * @param grid        updated grid (unused here — GameView handles rendering)
     * @param state       current {@link GameState}
     * @param scoreDelta  raw merge score for this move
     * @param spawnRow    spawn position row (unused here)
     * @param spawnCol    spawn position col (unused here)
     */
    @Override
    public void onBoardChanged(int[][] grid, GameState state, long scoreDelta,
                               int spawnRow, int spawnCol) {
        long effective = multiplierActive ? scoreDelta * 2 : scoreDelta;
        scoreManager.addScore(effective);

        gameView.updateScoreDisplay(scoreManager.getCurrentScore(),
                                    scoreManager.getBestScore());

        if (state == GameState.WON || state == GameState.LOST) {
            Platform.runLater(this::endGame);
        }
    }

    // ── Key handling ───────────────────────────────────────────────────────

    /**
     * Handles arrow-key moves and Ctrl+Z for undo.
     *
     * @param event the key event from the game scene
     */
    private void handleKeyPress(KeyEvent event) {
        if (!gameActive) return;

        KeyCode code = event.getCode();

        // F11 — toggle fullscreen
        if (code == KeyCode.F11) {
            Stage stage = (Stage) gameScene.getWindow();
            if (stage != null) stage.setFullScreen(!stage.isFullScreen());
            event.consume();
            return;
        }

        // Ctrl+Z — undo
        if (code == KeyCode.Z && event.isControlDown()) {
            Platform.runLater(this::handleUndo);
            event.consume();
            return;
        }

        // Ignore any non-arrow key
        if (code != KeyCode.LEFT  && code != KeyCode.RIGHT &&
            code != KeyCode.UP    && code != KeyCode.DOWN) return;

        // Consume the event so focused buttons don't intercept arrow keys
        event.consume();

        Platform.runLater(() -> {
            undoManager.saveState(board.getGrid(), scoreManager.getCurrentScore());
            switch (code) {
                case LEFT  -> board.moveLeft();
                case RIGHT -> board.moveRight();
                case UP    -> board.moveUp();
                case DOWN  -> board.moveDown();
            }
        });
    }

    // ── Undo ───────────────────────────────────────────────────────────────

    /**
     * Undoes the last move if possible: restores the board grid and score,
     * and updates the view's undo counter.
     */
    private void handleUndo() {
        if (!undoManager.canUndo()) return;

        UndoManager.UndoState saved = undoManager.undo();
        if (saved == null) return;

        board.restoreGrid(saved.grid());
        scoreManager.setScore(saved.score());

        gameView.updateScoreDisplay(scoreManager.getCurrentScore(),
                                    scoreManager.getBestScore());
        gameView.updateUndoCount(undoManager.getUndosRemaining());
    }

    // ── Time Challenge ─────────────────────────────────────────────────────

    /**
     * Starts the 60-second countdown timer for Time Challenge mode.
     * Updates the view every second and ends the game when time expires.
     */
    private void startCountdown() {
        timeRemaining = TIME_LIMIT_SECONDS;

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            gameView.updateTimer(timeRemaining);
            if (timeRemaining <= 0) {
                Platform.runLater(this::endGame);
            }
        }));
        countdownTimer.setCycleCount(TIME_LIMIT_SECONDS);
        countdownTimer.play();
    }

    /**
     * Schedules a ×2 score-multiplier event every {@value #MULTIPLIER_INTERVAL}
     * seconds. When activated, the grid glows gold and all merges during the
     * {@value #MULTIPLIER_DURATION}-second window score double.
     */
    private void startMultiplierScheduler() {
        multiplierScheduler = new Timeline(new KeyFrame(
                Duration.seconds(MULTIPLIER_INTERVAL), e -> {
            if (!gameActive) return;
            multiplierActive = true;
            gameView.showMultiplierEffect(true);

            // Deactivate after MULTIPLIER_DURATION seconds
            multiplierTimer = new Timeline(
                    new KeyFrame(Duration.seconds(MULTIPLIER_DURATION), e2 -> {
                        multiplierActive = false;
                        gameView.showMultiplierEffect(false);
                    })
            );
            multiplierTimer.play();
        }));
        multiplierScheduler.setCycleCount(Timeline.INDEFINITE);
        multiplierScheduler.play();
    }

    // ── End game ───────────────────────────────────────────────────────────

    /**
     * Ends the current game: stops timers, deactivates the flag, and
     * delegates to {@link MenuController} to show the end-game screen.
     */
    private void endGame() {
        if (!gameActive) return;
        gameActive = false;
        stopTimers();

        boolean won = board.getState() == GameState.WON;
        long    finalScore = scoreManager.getCurrentScore();

        menuController.showEndGame(finalScore, mode, won);
    }

    /** Stops all running {@link Timeline} timers safely. */
    private void stopTimers() {
        if (countdownTimer      != null) countdownTimer.stop();
        if (multiplierScheduler != null) multiplierScheduler.stop();
        if (multiplierTimer     != null) multiplierTimer.stop();
        multiplierActive = false;
    }
}

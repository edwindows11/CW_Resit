package com.example.demo.controller;

import com.example.demo.model.GameMode;
import com.example.demo.model.Leaderboard;
import com.example.demo.ui.EndGameView;
import com.example.demo.ui.LeaderboardView;
import com.example.demo.ui.MenuView;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Central scene-management controller for the application.
 *
 * <p>Holds the primary {@link Stage} and provides methods for switching
 * between the main menu, game, end-game, and leaderboard scenes. All
 * transitions go through this single class, keeping view and controller
 * code decoupled.</p>
 */
public class MenuController {

    // ── State ──────────────────────────────────────────────────────────────

    private final Stage           primaryStage;
    private       GameController  activeGame;

    // ── Constructor ────────────────────────────────────────────────────────

    /**
     * Creates the controller for the given application stage.
     *
     * @param primaryStage the JavaFX primary stage to manage
     */
    public MenuController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // ── Public navigation methods ──────────────────────────────────────────

    /**
     * Displays the main menu. Stops any currently running game first.
     */
    public void showMenu() {
        if (activeGame != null) {
            activeGame.stop();
            activeGame = null;
        }

        MenuView view = new MenuView(this::startGame, this::showLeaderboard);
        primaryStage.setScene(new Scene(view, 800, 800));
    }

    /**
     * Starts a new game in the given mode, replacing the current scene.
     *
     * @param mode the {@link GameMode} to start
     */
    public void startGame(GameMode mode) {
        activeGame = new GameController(mode, this);
        primaryStage.setScene(activeGame.createScene());
        activeGame.start();
    }

    /**
     * Shows the end-game screen with the player's result.
     *
     * @param score the final score achieved
     * @param mode  the mode that was played
     * @param won   {@code true} if the player reached the win tile
     */
    public void showEndGame(long score, GameMode mode, boolean won) {
        activeGame = null;

        EndGameView view = new EndGameView(
                score, mode, won,
                (name, s) -> {                         // Save & back to menu
                    Leaderboard.getInstance().addEntry(name, s, mode);
                    showMenu();
                },
                () -> startGame(mode)                  // Play again
        );

        primaryStage.setScene(new Scene(view, 800, 800));
    }

    /**
     * Displays the leaderboard screen.
     */
    public void showLeaderboard() {
        LeaderboardView view = new LeaderboardView(this::showMenu);
        primaryStage.setScene(new Scene(view, 800, 800));
    }

    /**
     * Returns the primary stage managed by this controller.
     *
     * @return the primary {@link Stage}
     */
    public Stage getPrimaryStage() { return primaryStage; }
}

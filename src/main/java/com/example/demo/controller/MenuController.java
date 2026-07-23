package com.example.demo.controller;

import com.example.demo.model.GameMode;
import com.example.demo.model.Leaderboard;
import com.example.demo.ui.EndGameView;
import com.example.demo.ui.LeaderboardView;
import com.example.demo.ui.MenuView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MenuController {

    private final Stage          primaryStage;
    private       GameController activeGame;

    public MenuController(Stage primaryStage) { this.primaryStage = primaryStage; }

    public void showMenu() {
        if (activeGame != null) { activeGame.stop(); activeGame = null; }
        primaryStage.setScene(new Scene(new MenuView(this::startGame, this::showLeaderboard), 820, 820));
    }

    public void startGame(GameMode mode) {
        activeGame = new GameController(mode, this);
        primaryStage.setScene(activeGame.createScene());
        activeGame.start();
    }

    public void showEndGame(long score, GameMode mode, boolean won) {
        activeGame = null;
        EndGameView view = new EndGameView(score, mode, won,
                (name, s) -> { Leaderboard.getInstance().addEntry(name, s, mode); showMenu(); },
                () -> startGame(mode));
        primaryStage.setScene(new Scene(view, 820, 820));
    }

    public void showLeaderboard() {
        primaryStage.setScene(new Scene(new LeaderboardView(this::showMenu), 820, 820));
    }

    public Stage getPrimaryStage() { return primaryStage; }
}

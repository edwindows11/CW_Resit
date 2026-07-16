package com.example.demo;

import com.example.demo.controller.MenuController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Application entry point for the 2048 game.
 *
 * <p>Bootstraps JavaFX, creates the primary {@link Stage}, and hands
 * control to {@link MenuController}, which manages all subsequent
 * scene transitions.</p>
 *
 * <p>Refactored from the original: the original {@code start()} method
 * contained hundreds of lines of UI setup, unused scene creation, and
 * dead code (Scanner, unused roots). All of that has been extracted into
 * dedicated View and Controller classes.</p>
 */
public class Main extends Application {

    /**
     * JavaFX lifecycle entry point. Sets up the stage and launches the menu.
     *
     * @param primaryStage the primary window provided by the JavaFX runtime
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("2048  —  press F11 for fullscreen");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(820);
        primaryStage.setMinHeight(820);

        MenuController controller = new MenuController(primaryStage);
        controller.showMenu();

        primaryStage.show();
    }

    /**
     * Standard Java entry point — delegates to {@link Application#launch(String...)}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}

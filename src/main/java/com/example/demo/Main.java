package com.example.demo;

import com.example.demo.controller.MenuController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

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

    public static void main(String[] args) { launch(args); }
}

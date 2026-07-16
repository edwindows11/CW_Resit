/**
 * Module definition for the 2048 game application.
 *
 * <p>Requires JavaFX controls and graphics modules and exports all
 * game packages so they are accessible to the JavaFX runtime.</p>
 */
module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    // Allow JavaFX reflection access for FXML and scene-graph internals
    opens com.example.demo            to javafx.graphics, javafx.fxml;
    opens com.example.demo.ui         to javafx.graphics, javafx.fxml;
    opens com.example.demo.controller to javafx.graphics, javafx.fxml;

    exports com.example.demo;
    exports com.example.demo.core;
    exports com.example.demo.ui;
    exports com.example.demo.controller;
    exports com.example.demo.model;
    exports com.example.demo.util;
}
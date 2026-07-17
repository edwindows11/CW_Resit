module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.demo to javafx.fxml;
    exports com.example.demo;
    exports com.example.demo.core;
    exports com.example.demo.model;
    exports com.example.demo.ui;
    exports com.example.demo.controller;
    exports com.example.demo.util;
    opens com.example.demo.core to javafx.fxml;
    opens com.example.demo.ui to javafx.fxml;
}
module com.maze {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    exports com.maze;
    exports com.maze.model;
    exports com.maze.algorithm;
    exports com.maze.view;
    exports com.maze.controller;
}

package com.maze;
import com.maze.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import java.util.Objects;

public class MazeApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainView mainView = new MainView();

        Scene scene = new Scene(mainView, 1100, 720);
        scene.getStylesheets().add(
            Objects.requireNonNull(
                getClass().getResource("/com/maze/styles.css")
            ).toExternalForm()
        );
       
        
        primaryStage.initStyle(StageStyle.UNDECORATED);

        
        primaryStage.setTitle("Maze Visualizer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
        primaryStage.setMaximized(true);

    }

    public static void main(String[] args) {
        launch();
    }
}

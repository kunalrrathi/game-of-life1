package org.gameoflife;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        GameController controller = new GameController();

        Scene scene = new Scene(controller.getRoot(), 1000, 800);
        stage.setTitle("Game of Life - JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

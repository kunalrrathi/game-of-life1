package utilities;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class BoardCoordinateCollector extends Application {

    @Override
    public void start(Stage stage) {

        Pane boardPane = new Pane();

        // Load board image
        ImageView boardImage = new ImageView(
                new Image(getClass().getResource("/org/gameoflife/life_board.jpg").toExternalForm())
        );

        boardPane.setStyle("-fx-border-color: red;");

        // Fix board size so coordinates remain stable
        boardImage.setFitWidth(1400);
        boardImage.setFitHeight(980);

        boardPane.getChildren().add(boardImage);

        // Capture mouse clicks
        boardPane.setOnMouseClicked(e -> {

            double x = e.getX();
            double y = e.getY();

            System.out.println("{" + (int)x + ", " + (int)y + "},");

            // Step 7: draw marker where user clicked
            Circle marker = new Circle(5, Color.RED);
            marker.setCenterX(x);
            marker.setCenterY(y);

            boardPane.getChildren().add(marker);
        });

        Scene scene = new Scene(boardPane);

        stage.setTitle("Board Coordinate Collector");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
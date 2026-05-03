package org.gameoflife;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class GameLogPanel {

    private final TextFlow logFlow;
    private final ScrollPane scrollPane;

    public GameLogPanel() {

        logFlow = new TextFlow();
        logFlow.setStyle("-fx-padding: 10;");

        scrollPane = new ScrollPane(logFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);

        scrollPane.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;"
        );
    }

    // 🔥 Main logging method (with color)
    public void log(String message, String color) {

        Text text = new Text(message + "\n");

        text.setStyle(
                "-fx-fill: " + color + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-family: monospace;"
        );

        logFlow.getChildren().add(text);

        // 🔥 LIMIT SIZE (prevents memory issue)
        if (logFlow.getChildren().size() > 200) {
            logFlow.getChildren().remove(0, 50);
        }

        // 🔥 Auto-scroll
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }

    // 🔹 Optional fallback (default color)
    public void log(String message) {
        log(message, "#000000");
    }

    public ScrollPane getView() {
        return scrollPane;
    }
}
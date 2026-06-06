package org.gameoflife;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class MainGameLayout {

    private final BorderPane root;

    public MainGameLayout(
            Node boardView,
            Node playerPanel,
            Node logPanel
    ) {

        root = new BorderPane();

        // =====================================================
        // CENTER SECTION
        // =====================================================

        BorderPane rightPanel = new BorderPane();
        rightPanel.setPrefWidth(450);

        rightPanel.setCenter(playerPanel);
        rightPanel.setBottom(logPanel);

        HBox mainContent = new HBox(20);

        HBox.setHgrow(boardView, Priority.ALWAYS);

        mainContent.getChildren().addAll(
                boardView,
                rightPanel
        );

        root.setCenter(mainContent);
    }

    public BorderPane getRoot() {
        return root;
    }
}
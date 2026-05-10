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

        HBox centerSection = new HBox(20);

        centerSection.setPadding(new Insets(10));

        // Board grows naturally
        HBox.setHgrow(boardView, Priority.ALWAYS);

        centerSection.getChildren().addAll(
                boardView,
                playerPanel
        );

        // =====================================================
        // ROOT LAYOUT
        // =====================================================

        root.setCenter(centerSection);

        root.setBottom(logPanel);
    }

    public BorderPane getRoot() {
        return root;
    }
}
package org.gameoflife;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class PlayersDashboard {

    private VBox panel;

    private List<PlayerCard> playerCards;

    public PlayersDashboard() {

        panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(240);

        panel.setStyle("-fx-background-color: #f4f4f4;");

        playerCards = new ArrayList<>();
    }

    public void addPlayer(String name) {

        PlayerCard card = new PlayerCard(name);

        playerCards.add(card);

        panel.getChildren().add(card.getCard());
    }

    public VBox getPanel() {
        return panel;
    }
}
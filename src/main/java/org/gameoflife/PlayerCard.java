package org.gameoflife;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PlayerCard {

    private VBox card;

    private Label nameLabel;
    private Label cashLabel;
    private Label notesLabel;

    private Label fireInsurance;
    private Label autoInsurance;
    private Label stockInsurance;
    private Label lifeInsurance;

    private Label marriedLabel;
    private Label childrenLabel;

    private Label collectCards;
    private Label payCards;
    private Label exemptionCards;

    public PlayerCard(String playerName) {

        card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(220);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        nameLabel = new Label(playerName);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        cashLabel = new Label("Cash: $10000");
        notesLabel = new Label("Notes: 0");

        Label insuranceTitle = new Label("Insurance");
        fireInsurance = new Label("Fire: 0");
        autoInsurance = new Label("Auto: 0");
        stockInsurance = new Label("Stock: 0");
        lifeInsurance = new Label("Life: 0");

        Label familyTitle = new Label("Family");
        marriedLabel = new Label("Married: No");
        childrenLabel = new Label("Children: 0");

        Label cardsTitle = new Label("Share The Wealth");
        collectCards = new Label("Collect: 0");
        payCards = new Label("Pay: 0");
        exemptionCards = new Label("Exemption: 0");

        card.getChildren().addAll(
                nameLabel,
                cashLabel,
                notesLabel,

                insuranceTitle,
                fireInsurance,
                autoInsurance,
                stockInsurance,
                lifeInsurance,

                familyTitle,
                marriedLabel,
                childrenLabel,

                cardsTitle,
                collectCards,
                payCards,
                exemptionCards
        );
    }

    public VBox getCard() {
        return card;
    }

    public void update(Player player) {

        cashLabel.setText("Cash: $" + player.getCash());
        notesLabel.setText("Notes: " + player.getPromissoryNotes());
        fireInsurance.setText("Fire: " + (player.hasFireInsurance() ? "Yes" : "No"));
        autoInsurance.setText("Auto: " + (player.hasAutoInsurance() ? "Yes" : "No"));
        stockInsurance.setText("Stock: " + (player.hasStockInsurance() ? "Yes" : "No"));
        lifeInsurance.setText("Life: " + (player.isLifeInsurance() ? "Yes" : "No"));
        marriedLabel.setText("Married: " + (player.isMarried() ? "Yes" : "No"));
        childrenLabel.setText("Children: " + player.getChildren());

        // Add more later:
        // insuranceLabel.setText(...)
        // childrenLabel.setText(...)
    }
}
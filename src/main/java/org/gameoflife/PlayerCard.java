package org.gameoflife;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class PlayerCard {

    private final Player player;
    private final PlayerToken token;

    private VBox card;

    private Region colorStrip;
    private Label professionLabel;

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

    private static final String BASE_STYLE =
            "-fx-background-color: white;" +
                    "-fx-border-color: #dddddd;" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 4);";

    public PlayerCard(
            Player player,
            PlayerToken token
    ) {
        colorStrip = new Region();

        colorStrip.setPrefHeight(8);

        colorStrip.setStyle(
                "-fx-background-color: " + toRgbString(token.getColor()) + ";" +
                        "-fx-background-radius: 6 6 0 0;"
        );

        card = new VBox(8);
        card.setPadding(new Insets(10));
        card.setPrefWidth(170);
        card.setPrefHeight(260);

        card.setStyle(BASE_STYLE);

        nameLabel = new Label(player.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        professionLabel = new Label(player.getProfessionDisplayName());
        professionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

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
                colorStrip,
                nameLabel,
                professionLabel,
                cashLabel,
                notesLabel,

                insuranceTitle,
                fireInsurance,
                autoInsurance,
                stockInsurance,
                lifeInsurance,

                familyTitle,
                marriedLabel,
                childrenLabel

//                cardsTitle,
//                collectCards,
//                payCards,
//                exemptionCards
        );
        this.player = player;
        this.token = token;
    }

    public VBox getCard() {
        return card;
    }

    public void update(Player player) {
        professionLabel.setText(player.getProfessionDisplayName());
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

    public void setActive(boolean active) {

        if (active) {

            card.setStyle(
                    BASE_STYLE +
                            "-fx-border-color: " + toRgbString(token.getColor()) + ";" +
                            "-fx-border-width: 3;" +
                            "-fx-effect: dropshadow(gaussian, " + toRgbString(token.getColor()) + ", 18, 0.4, 0, 0);"
            );

        } else {

            card.setStyle(BASE_STYLE);
        }
    }

    private String toRgbString(Color color) {

        return String.format(
                "rgb(%d,%d,%d)",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255)
        );
    }

    public Player getPlayer() {
        return player;
    }
}
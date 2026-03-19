package org.gameoflife;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameSetupDialog {

    public static List<Player> showDialog() {

        Dialog<List<Player>> dialog = new Dialog<>();
        dialog.setTitle("Game Setup");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextField playerCountField = new TextField();
        playerCountField.setPromptText("Enter number of players");

        VBox insuranceBox = new VBox(5);

        root.getChildren().addAll(
                new Label("Number of Players"),
                playerCountField,
                new Label("Car Insurance"),
                insuranceBox
        );

        // Update checkboxes dynamically
        playerCountField.textProperty().addListener((obs, oldVal, newVal) -> {

            insuranceBox.getChildren().clear();

            try {

                int count = Integer.parseInt(newVal);

                for (int i = 1; i <= count; i++) {

                    CheckBox cb = new CheckBox("Player " + i + " - Car Insurance ($1000)");

                    insuranceBox.getChildren().add(cb);
                }
                dialog.getDialogPane().getScene().getWindow().sizeToScene();

            } catch (Exception ignored) {}

        });

        dialog.getDialogPane().setContent(root);

        ButtonType startButton = new ButtonType("Start Game", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(startButton, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {

            if (button == startButton) {

                List<Player> players = new ArrayList<>();

                int count = Integer.parseInt(playerCountField.getText());

                for (int i = 0; i < count; i++) {

                    CheckBox cb = (CheckBox) insuranceBox.getChildren().get(i);

                    Player p = new Player("Player " + (i + 1));

                    if (cb.isSelected()) {
                        p.setAutoInsurance(true);
                        p.pay(1000);
                    }

                    players.add(p);
                }

                return players;
            }

            return null;
        });

        Optional<List<Player>> result = dialog.showAndWait();

        return result.orElse(new ArrayList<>());
    }
}
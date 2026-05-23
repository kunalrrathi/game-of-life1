package org.gameoflife;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class GameSetupDialog {

    private static final List<String> AI_NAMES =
            new ArrayList<>(List.of(
                "Neha 🤖",
                "Nikhil 🤖",
                "Nitya 🤖",
                "Sharayu 🤖",
                "Mummyji 🤖",
                "Papaji 🤖"
            ));

    private static String getRandomAIName() {

        if (AI_NAMES.isEmpty()) {
            return "Computer 🤖";
        }

        int index =
                new Random().nextInt(AI_NAMES.size());

        return AI_NAMES.remove(index);
    }

    public static List<Player> showDialog(GameLogPanel logPanel) {

        Dialog<List<Player>> dialog = new Dialog<>();
        dialog.setTitle("Game Setup");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TextField playerCountField = new TextField();
        playerCountField.setPromptText("Enter number of players");

        VBox playerConfigBox = new VBox(8);
        List<PlayerSetupRow> playerRows = new ArrayList<>();

        root.getChildren().addAll(
                new Label("Number of Players"),
                playerCountField,
                new Label("Player Configuration"),
                playerConfigBox
        );

        // 🔄 Dynamically update player rows
        playerCountField.textProperty().addListener((obs, oldVal, newVal) -> {

            playerConfigBox.getChildren().clear();
            playerRows.clear();

            try {
                int count = Integer.parseInt(newVal);

                for (int i = 1; i <= count; i++) {

                    int playerIndex = i;

                    Label label = new Label("Player " + playerIndex);

                    TextField nameField = new TextField();
                    nameField.setPromptText("Enter name");
                    nameField.setText("Player " + playerIndex); // default

                    CheckBox insuranceCB = new CheckBox("Car Insurance ($1000)");
                    CheckBox computerCB = new CheckBox("Computer Player");

                    // 🤖 Toggle behavior
                    computerCB.selectedProperty().addListener((obs2, wasSelected, isNowSelected) -> {

                        if (isNowSelected) {
                            nameField.setText(getRandomAIName());
                            nameField.setDisable(true);
                            insuranceCB.setDisable(true);
                        } else {
                            nameField.setDisable(false);
                            insuranceCB.setDisable(false);
                            nameField.setText("Player " + playerIndex);
                        }
                    });

                    VBox playerRowUI = new VBox(3,
                            label,
                            new Label("Name"),
                            nameField,
                            insuranceCB,
                            computerCB
                    );

                    playerRows.add(new PlayerSetupRow(nameField, insuranceCB, computerCB));
                    playerConfigBox.getChildren().add(playerRowUI);
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

                    PlayerSetupRow row = playerRows.get(i);

                    String name = row.nameField.getText().trim();

                    if (name.isEmpty()) {
                        name = "Player " + (i + 1);
                    }
                    Player p;
                    if (row.computerCheckBox.isSelected())
                        p = new Player(name);
                    else
                        p = new Player(name + " 👤");


                    // 🚗 Insurance
                    if (row.insuranceCheckBox.isSelected()) {
                        p.setAutoInsurance(true);
                        p.pay(1000);
                        logPanel.log(name + " bought car insurance for $1000");
                    }

                    // 🤖 Computer Player
                    if (row.computerCheckBox.isSelected()) {
                        p.setComputer(true);
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

    // 🧩 Helper class to keep UI clean
    static class PlayerSetupRow {
        TextField nameField;
        CheckBox insuranceCheckBox;
        CheckBox computerCheckBox;

        public PlayerSetupRow(TextField nameField, CheckBox insuranceCheckBox, CheckBox computerCheckBox) {
            this.nameField = nameField;
            this.insuranceCheckBox = insuranceCheckBox;
            this.computerCheckBox = computerCheckBox;
        }
    }
}
package org.gameoflife;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GameSummaryScreen {

    private VBox root;

    public GameSummaryScreen(List<Player> players, Runnable onRestart) {

        root = new VBox(15);
        root.setPadding(new Insets(20));

        // 🧠 Sort players by wealth
        List<Player> sortedPlayers = players.stream()
                .sorted(Comparator.comparingInt(this::calculateWealth).reversed())
                .collect(Collectors.toList());

        // 🏆 Winner
        Player winner = sortedPlayers.get(0);

        Label title = new Label("🎉 Game Summary");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label winnerLabel = new Label("🏆 Winner: " + winner.getName());
        winnerLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: green;");

        root.getChildren().addAll(title, winnerLabel);

        // 👇 Player Cards
        for (int i = 0; i < sortedPlayers.size(); i++) {

            Player p = sortedPlayers.get(i);

            PlayerCard card = new PlayerCard(p.getName());
            card.update(p);

            // 🥇 Highlight winner
            if (i == 0) {
                card.getCard().setStyle("-fx-background-color: gold;");
            }

            root.getChildren().add(card.getCard());
        }

        // 🔁 Restart button
        Button restartBtn = new Button("Restart Game");
        restartBtn.setOnAction(e -> onRestart.run());

        root.getChildren().add(restartBtn);
    }

    private int calculateWealth(Player p) {
        return p.getCash();
    }

    public VBox getRoot() {
        return root;
    }
}
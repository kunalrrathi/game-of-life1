package org.gameoflife;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GameSummaryScreen {

    private final VBox root;

    public GameSummaryScreen(
            List<Player> players,
            Runnable onRestart
    ) {

        root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom,#f8f9ff,#edf4ff);"
        );

        List<Player> sortedPlayers =
                players.stream()
                        .sorted(
                                Comparator.comparingInt(
                                        this::calculateWealth
                                ).reversed()
                        )
                        .collect(Collectors.toList());

        Player winner = sortedPlayers.get(0);

        // =====================================================
        // TITLE
        // =====================================================

        Label title =
                new Label("🏁 GAME OVER");

        title.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;"
        );

        Label winnerLabel =
                new Label("🏆 Winner: " + winner.getName());

        winnerLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-text-fill: green;" +
                        "-fx-font-weight: bold;"
        );

        Label victoryType =
                new Label("Victory Type: Highest Wealth");

        victoryType.setStyle(
                "-fx-font-size: 14px;"
        );

        root.getChildren().addAll(
                title,
                winnerLabel,
                victoryType
        );

        // =====================================================
        // PLAYER RANKINGS
        // =====================================================

        for (int i = 0; i < sortedPlayers.size(); i++) {

            Player player = sortedPlayers.get(i);

            VBox card =
                    createSummaryCard(player, i);

            root.getChildren().add(card);
        }

        // =====================================================
        // BUTTONS
        // =====================================================

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button restart =
                new Button("🔁 Play Again");

        restart.setOnAction(e -> onRestart.run());

        Button close =
                new Button("❌ Close");

        close.setOnAction(e ->
                root.getScene()
                        .getWindow()
                        .hide()
        );

        buttons.getChildren().addAll(
                restart,
                close
        );

        root.getChildren().add(buttons);
    }

    // =====================================================
    // PLAYER CARD
    // =====================================================

    private VBox createSummaryCard(
            Player p,
            int rank
    ) {

        VBox card = new VBox(5);
        card.setPadding(new Insets(12));

        String medal =
                switch (rank) {
                    case 0 -> "🥇";
                    case 1 -> "🥈";
                    case 2 -> "🥉";
                    default -> "🎖";
                };

        if (rank == 0) {

            card.setStyle(
                    "-fx-background-color: gold;" +
                            "-fx-background-radius: 10;"
            );

        } else {

            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: lightgray;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;"
            );
        }

        Label name =
                new Label(
                        medal + " " + p.getName()
                );

        name.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );

        Label status =
                new Label(
                        "Status: " + getStatus(p)
                );

        Label cash =
                new Label(
                        "Cash: ₹" + p.getCash()
                );

        Label stock =
                new Label(
                        "Stock Value: ₹" +
                                (p.hasStock() ? 120000 : 0)
                );

        Label insurance =
                new Label(
                        "Life Insurance: ₹" +
                                (p.isLifeInsurance() ? 8000 : 0)
                );

        Label total =
                new Label(
                        "Total Wealth: ₹" +
                                calculateWealth(p)
                );

        total.setStyle(
                "-fx-font-weight: bold;"
        );

        card.getChildren().addAll(
                name,
                status,
                cash,
                stock,
                insurance,
                total
        );

        return card;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String getStatus(Player p) {

        if (p.isBankrupt()) {
            return "Bankrupt";
        }

        if (p.isRetired()) {
            return "Millionaire";
        }

        return "Active";
    }

    private int calculateWealth(Player p) {

        int total = p.getCash();

        if (p.hasStock()) {
            total += 120000;
        }

        if (p.isLifeInsurance()) {
            total += 8000;
        }

        return total;
    }

    public VBox getRoot() {
        return root;
    }
}
package org.gameoflife;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NormalSpaceHandler {

    private final PlayersDashboard dashboard;
    private final SpinnerController spinnerController;
    private final List<Player> players;
    private final MovementController movementController;
    private final Runnable onDecisionStart;
    private final Runnable onDecisionEnd;

    public NormalSpaceHandler(
            PlayersDashboard dashboard,
            SpinnerController spinnerController,
            List<Player> players,
            MovementController movementController,
            Runnable onDecisionStart,
            Runnable onDecisionEnd
    ) {
        this.dashboard = dashboard;
        this.spinnerController = spinnerController;
        this.players = players;
        this.movementController = movementController;
        this.onDecisionStart = onDecisionStart;
        this.onDecisionEnd = onDecisionEnd;
    }

    public void handle(Player player, BoardSpace space, String action) {

        if (action == null) return;

        action = action.trim();

        switch (action) {

            case "Collect":
                player.collect(space.getAmount());
                break;

            case "Pay":
                player.pay(space.getAmount());
                break;

            case "Collect-Life":
                if (player.isLifeInsurance()) {
                    player.collect(space.getAmount());
                }
                break;

            case "Collect-Stock":
                if (player.hasStockInsurance()) {
                    player.collect(space.getAmount());
                } else
                    System.out.println(player.getName() + " has no Stock Insurance, so no collection.");
                break;

            case "Pay-Car":
                if (!player.hasAutoInsurance()) {
                    player.pay(space.getAmount());
                } else
                    System.out.println(player.getName() + " has Auto Insurance, so no payment needed.");
                break;

            case "Pay-Fire":
                if (!player.hasFireInsurance()) {
                    player.pay(space.getAmount());
                } else
                    System.out.println(player.getName() + " has Fire Insurance, so no payment needed.");
                break;

            case "Pay-Stock":
                if (player.hasStockInsurance()) {
                    player.pay(space.getAmount());
                } else
                    System.out.println(player.getName() + " has Stock Insurance, so no payment needed.");
                break;

            case "Wait-Turn":
                System.out.println(player.getName() + " loses next turn.");
                player.setSkipTurns(1);
                break;

            case "Child":
                handleChild(player, 1);
                break;

            case "Twins":
                handleChild(player, 2);
                break;

            case "Retire":
                player.setRetired(true);
                break;

            case "Revenge":
                handleRevenge(player);
                break;

            case "Spin-Again":
                System.out.println(player.getName() + " gets another spin!");
                break;

            case "Collect-Spin-3": //If you spin 3 collect $3000
            case "Collect-Spin-8": //If you spin 3 collect $8000
            case "Collect-Spin-the-Wheel":
                System.out.println("Spin reward logic pending...");
                break;

            case "Lucky-Day":
                handleLuckyDay(player);
                break;

            case "Detour":
                System.out.println("Detour logic pending...");
                break;
        }

        dashboard.refresh(List.of(player));
    }

    private void handleChild(Player player, int count) {

        for (int i = 0; i < count; i++) {
            player.addChild();
        }

        int amountPerOpponent = count * 1000;

        System.out.println(
                "👶 " + player.getName() +
                        " had " + (count == 1 ? "a child" : "twins") +
                        " and collected ₹" + amountPerOpponent +
                        " from each player"
        );

        for (Player p : players) {

            if (p == player) continue;

            p.pay(amountPerOpponent);
            player.collect(amountPerOpponent);
        }

        dashboard.refresh(players);
    }

    private void handleRevenge(Player player) {
        onDecisionStart.run();

        List<Player> targets = players.stream()
                .filter(p -> p != player)
                .filter(p -> !p.isRetired())
                .toList();

        if (targets.isEmpty()) return;

        if (player.isComputer()) {

            Player target = targets.get(new Random().nextInt(targets.size()));

            if (target.getCash() >= 200000) {
                takeMoney(player, target);
            } else {
                sendBack(target);
            }

            return;
        }

        // 👤 Human UI
        Platform.runLater(() -> {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Revenge");
            alert.setHeaderText("Choose your revenge");

            ButtonType takeMoney = new ButtonType("Take ₹200,000");
            ButtonType sendBack = new ButtonType("Send back 10 spaces");

            alert.getButtonTypes().setAll(takeMoney, sendBack);

            alert.showAndWait().ifPresent(choice -> {

                chooseTarget(player, targets, choice == takeMoney);
            });
        });
    }

    private void chooseTarget(Player attacker, List<Player> targets, boolean takeMoney) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Choose Player");

        List<ButtonType> buttons = new ArrayList<>();

        for (Player p : targets) {
            buttons.add(new ButtonType(p.getName()));
        }

        alert.getButtonTypes().setAll(buttons);

        alert.showAndWait().ifPresent(choice -> {

            Player target = targets.stream()
                    .filter(p -> p.getName().equals(choice.getText()))
                    .findFirst()
                    .orElse(null);

            if (target == null) return;

            if (takeMoney && target.getCash() >= 200000) {
                takeMoney(attacker, target);
            } else {
                sendBack(target);
            }
        });
    }

    private void takeMoney(Player attacker, Player target) {
        onDecisionEnd.run();

        target.pay(200000);
        attacker.collect(200000);

        System.out.println(attacker.getName() + " took ₹200000 from " + target.getName());

        dashboard.refresh(players);
    }

    private void sendBack(Player target) {
        onDecisionEnd.run();

        System.out.println(target.getName() + " is sent back 10 spaces!");

        PlayerToken token =
                movementController.getTokenForPlayer(target);

        int newIndex = Math.max(0, token.getCurrentIndex() - 10);

        token.setCurrentIndex(newIndex);

        BoardSpace space = movementController.getBoard().getSpace(newIndex);

        token.getNode().setLayoutX(space.getX());
        token.getNode().setLayoutY(space.getY());

        dashboard.refresh(List.of(target));
    }

    private void handleLuckyDay(Player player) {

        player.collect(20000);

        System.out.println(player.getName() + " landed on Lucky Day and received ₹20000");

        onDecisionStart.run();

        if (player.isComputer()) {

            // 🤖 AI decision
            boolean speculate = new Random().nextBoolean();

            if (!speculate) {
                finishLuckyDay();
                return;
            }

            int n1 = new Random().nextInt(10) + 1;
            int n2;

            do {
                n2 = new Random().nextInt(10) + 1;
            } while (n2 == n1);

            performLuckySpin(player, n1, n2);

            return;
        }

        // 👤 Human choice
        Platform.runLater(() -> {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Lucky Day");
            alert.setHeaderText("You got ₹20,000!");

            ButtonType keep = new ButtonType("Keep Money");
            ButtonType speculate = new ButtonType("Speculate");

            alert.getButtonTypes().setAll(keep, speculate);

            alert.showAndWait().ifPresent(choice -> {

                if (choice == keep) {
                    finishLuckyDay();
                } else {
                    chooseLuckyNumbers(player);
                }
            });
        });
    }

    private void chooseLuckyNumbers(Player player) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Choose 2 Numbers");

        List<ButtonType> buttons = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            buttons.add(new ButtonType(String.valueOf(i)));
        }

        alert.getButtonTypes().setAll(buttons);

        alert.showAndWait().ifPresent(choice1 -> {

            int n1 = Integer.parseInt(choice1.getText());

            Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION);
            alert2.setTitle("Choose second number");

            List<ButtonType> buttons2 = new ArrayList<>();

            for (int i = 1; i <= 10; i++) {
                if (i != n1) {
                    buttons2.add(new ButtonType(String.valueOf(i)));
                }
            }

            alert2.getButtonTypes().setAll(buttons2);

            alert2.showAndWait().ifPresent(choice2 -> {

                int n2 = Integer.parseInt(choice2.getText());

                performLuckySpin(player, n1, n2);
            });
        });
    }

    private void performLuckySpin(Player player, int n1, int n2) {

        System.out.println(player.getName() + " is betting on " + n1 + " & " + n2);

        spinnerController.spin(result -> {

            System.out.println("Lucky Spin result: " + result);

            if (result == n1 || result == n2) {

                player.collect(300000);

                System.out.println("🎉 Lucky Win! ₹300000");

            } else {

                System.out.println("No win in Lucky Day.");
            }

            dashboard.refresh(players);

            finishLuckyDay();
        });
    }

    private void finishLuckyDay() {
        onDecisionEnd.run();
    }

}
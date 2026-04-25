package org.gameoflife;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class StopSpaceHandler {

    public interface StopCallback {
        void enableSpin();
        void continueMovement(Player player, int steps);
        void requestSpinForStop();
        void endTurn();
        void endGame(Player winner);
    }

    private final StopCallback callback;
    private final PlayersDashboard dashboard;

    // =========================================================
    // 🎯 MARRIAGE FLOW
    // =========================================================

    private enum MarriageStage {
        NONE,
        WAITING_FOR_GIFT_SPIN,
        WAITING_FOR_HONEYMOON_SPIN
    }

    private MarriageStage marriageStage = MarriageStage.NONE;
    private Player marriagePlayer;

    // =========================================================
    // 🎯 TYCOON FLOW
    // =========================================================

    private boolean waitingForTycoonSpin = false;
    private Player tycoonPlayer;
    private int chosenTycoonNumber;

    public StopSpaceHandler(
            PlayersDashboard dashboard,
            StopCallback callback
    ) {
        this.dashboard = dashboard;
        this.callback = callback;
    }

    // =========================================================
    // 🎯 STATE
    // =========================================================

    public boolean isInProgress() {
        return marriageStage != MarriageStage.NONE
                || waitingForTycoonSpin;
    }

    // =========================================================
    // 🎯 ENTRY
    // =========================================================

    public void handle(Player player, BoardSpace space) {

        String action = space.getAction();

        if (action == null) return;

        switch (action.trim()) {

            case "Marriage":
                handleMarriage(player);
                break;

            case "Reckoning":
                handleReckoning(player);
                break;
        }
    }

    // =========================================================
    // 🎯 SHARED SPINNER RESULT
    // =========================================================

    public void handleSpin(int spin) {

        System.out.println("Stop Space Spin: " + spin);

        // -----------------------------------------------------
        // TYCOON RESULT
        // -----------------------------------------------------
        if (waitingForTycoonSpin) {

            waitingForTycoonSpin = false;

            if (spin == chosenTycoonNumber) {

                showResult(
                        "🎉 TYCOON!",
                        tycoonPlayer.getName()
                                + " becomes the WINNER!"
                );

                callback.endGame(tycoonPlayer);

            } else {

                tycoonPlayer.setCash(0);
                tycoonPlayer.setBankrupt(true);

                showResult(
                        "💀 BANKRUPT",
                        tycoonPlayer.getName()
                                + " lost everything!"
                );

                dashboard.refresh(List.of(tycoonPlayer));

                callback.endTurn();
            }

            dashboard.refresh(List.of(tycoonPlayer));
            return;
        }

        // -----------------------------------------------------
        // MARRIAGE GIFT SPIN
        // -----------------------------------------------------
        if (marriageStage
                == MarriageStage.WAITING_FOR_GIFT_SPIN) {

            int gift = getMarriageGiftAmount(spin);

            marriagePlayer.collect(gift);

            marriageStage =
                    MarriageStage.WAITING_FOR_HONEYMOON_SPIN;

            Platform.runLater(() -> {

                Alert alert =
                        new Alert(Alert.AlertType.INFORMATION);

                alert.setTitle("🎁 Wedding Gifts");
                alert.setHeaderText(
                        "You spun: " + spin
                );

                alert.setContentText(
                        "You received ₹" + gift
                                + "\n\nSpin again for honeymoon!"
                );

                alert.showAndWait();

                callback.enableSpin();
            });

            dashboard.refresh(List.of(marriagePlayer));
            return;
        }

        // -----------------------------------------------------
        // MARRIAGE HONEYMOON SPIN
        // -----------------------------------------------------
        if (marriageStage
                == MarriageStage.WAITING_FOR_HONEYMOON_SPIN) {

            marriageStage = MarriageStage.NONE;

            callback.continueMovement(
                    marriagePlayer,
                    spin
            );
        }
    }

    // =========================================================
    // 🎯 MARRIAGE
    // =========================================================

    private void handleMarriage(Player player) {

        player.setMarried(true);

        marriagePlayer = player;

        marriageStage =
                MarriageStage.WAITING_FOR_GIFT_SPIN;

        dashboard.refresh(List.of(player));

        if (player.isComputer()) {

            System.out.println(
                    player.getName()
                            + " (AI) got married!"
            );

            PauseTransition delay =
                    new PauseTransition(
                            Duration.seconds(1)
                    );

            delay.setOnFinished(
                    e -> callback.enableSpin()
            );

            delay.play();

        } else {

            Platform.runLater(() -> {

                Alert alert =
                        new Alert(Alert.AlertType.INFORMATION);

                alert.setTitle("💍 Marriage");
                alert.setHeaderText(
                        player.getName()
                                + " got married!"
                );

                alert.setContentText(
                        "Spin again to collect gifts!"
                );

                alert.showAndWait();

                callback.enableSpin();
            });
        }
    }

    private int getMarriageGiftAmount(int spin) {

        if (spin >= 1 && spin <= 3) return 2000;
        if (spin >= 4 && spin <= 6) return 1000;

        return 0;
    }

    // =========================================================
    // 🎯 DAY OF RECKONING
    // =========================================================

    private void handleReckoning(Player player) {

        System.out.println("DAY OF RECKONING!");

        // 1️⃣ Pay promissory notes
        player.settleLoansAtRetirement();

        // 2️⃣ Collect child reward
        int childBonus =
                player.getChildren() * 48000;

        if (childBonus > 0) {
            player.collect(childBonus);
        }

        dashboard.refresh(List.of(player));

        askRetirementChoice(player);
    }

    private void askRetirementChoice(Player player) {

        // 🤖 AI decision
        if (player.isComputer()) {

            if (player.getCash() >= 100000) {
                handleMillionairePath(player);
            } else {
                handleTycoon(player);
            }

            return;
        }

        // 👤 Human popup
        Platform.runLater(() -> {

            Alert alert =
                    new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle("Day of Reckoning");
            alert.setHeaderText(
                    "Choose your path"
            );

            ButtonType millionaire =
                    new ButtonType(
                            "Become Millionaire"
                    );

            ButtonType tycoon =
                    new ButtonType(
                            "Try Tycoon"
                    );

            alert.getButtonTypes().setAll(
                    millionaire,
                    tycoon
            );

            alert.showAndWait().ifPresent(choice -> {

                if (choice == millionaire) {
                    handleMillionairePath(player);
                } else {
                    handleTycoon(player);
                }
            });
        });
    }

    // =========================================================
    // 🎯 MILLIONAIRE PATH
    // =========================================================

    private void handleMillionairePath(Player player) {

        System.out.println(
                player.getName()
                        + " chose Millionaire path"
        );

        if (player.isComputer()) {
            callback.requestSpinForStop();   // AI auto spin
        } else {
            callback.enableSpin();          // Human manual click
        }
    }

    // =========================================================
    // 🎯 TYCOON PATH
    // =========================================================

    private void handleTycoon(Player player) {

        tycoonPlayer = player;

        // 🤖 AI auto-pick
        if (player.isComputer()) {

            chosenTycoonNumber =
                    new Random().nextInt(10) + 1;

            System.out.println(
                    player.getName()
                            + " chose number "
                            + chosenTycoonNumber
            );

            waitingForTycoonSpin = true;

            callback.requestSpinForStop();

            return;
        }

        // 👤 Human chooses number
        Platform.runLater(() -> {

            Alert alert =
                    new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle("Millionaire Tycoon");
            alert.setHeaderText(
                    "Choose a number (1 to 10)"
            );

            ButtonType[] buttons =
                    new ButtonType[10];

            for (int i = 1; i <= 10; i++) {
                buttons[i - 1] =
                        new ButtonType(
                                String.valueOf(i)
                        );
            }

            alert.getButtonTypes()
                    .setAll(buttons);

            alert.showAndWait().ifPresent(choice -> {

                chosenTycoonNumber =
                        Integer.parseInt(
                                choice.getText()
                        );

                waitingForTycoonSpin = true;

                callback.requestSpinForStop();
            });
        });
    }

    // =========================================================
    // 🎯 UI HELPERS
    // =========================================================

    private void showResult(
            String title,
            String message
    ) {

        Platform.runLater(() -> {

            Alert alert =
                    new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Result");
            alert.setHeaderText(title);
            alert.setContentText(message);

            alert.showAndWait();
        });
    }
}
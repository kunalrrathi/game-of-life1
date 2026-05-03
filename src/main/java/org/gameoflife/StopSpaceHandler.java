package org.gameoflife;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.List;
import java.util.Random;
import static utilities.LogColors.*;


public class StopSpaceHandler {

    private GameLogPanel logPanel;

    public interface StopCallback {
        void enableSpin();
        void continueMovement(Player player, int steps);
        void spinForStop(java.util.function.IntConsumer callback);
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

    private boolean waitingForReckoningSpin = false;
    private Player currentStopPlayer;

    public StopSpaceHandler(
            PlayersDashboard dashboard,
            StopCallback callback,
            GameLogPanel logPanel) {
        this.dashboard = dashboard;
        this.callback = callback;
        this.logPanel = logPanel;
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

                waitingForTycoonSpin = false;

                logPanel.log(tycoonPlayer.getName() + " became TYCOON!", WIN);

                showResult(
                        "🎉 TYCOON!",
                        tycoonPlayer.getName() + " becomes the WINNER!"
                );

                // 🔥 END GAME COMPLETELY
                callback.endGame(tycoonPlayer);

                return;
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
        if (marriageStage == MarriageStage.WAITING_FOR_GIFT_SPIN) {

            int gift = getMarriageGiftAmount(spin);

            marriagePlayer.collect(gift);

            marriageStage = MarriageStage.WAITING_FOR_HONEYMOON_SPIN;

            dashboard.refresh(List.of(marriagePlayer));

            if (marriagePlayer.isComputer()) {

                // 🤖 AI → NO popup, continue immediately
                callback.spinForStop(nextSpin -> handleSpin(nextSpin));

            } else {

                // 👤 Human → show popup, then spin again
                Platform.runLater(() -> {

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);

                    alert.setTitle("🎁 Wedding Gifts");
                    alert.setHeaderText("You spun: " + spin);

                    alert.setContentText(
                            "You received ₹" + gift +
                                    "\n\nSpin again for honeymoon!"
                    );

                    alert.showAndWait();

                    callback.spinForStop(nextSpin -> handleSpin(nextSpin));
                });
            }

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

        // -----------------------------------------------------
        // RECKONING → CONTINUE MOVEMENT
        // -----------------------------------------------------
        if (waitingForReckoningSpin) {

            waitingForReckoningSpin = false;

            callback.continueMovement(currentStopPlayer, spin);

            return;
        }
    }

    // =========================================================
    // 🎯 MARRIAGE
    // =========================================================
    private void handleMarriage(Player player) {

        player.setMarried(true);

        marriagePlayer = player;
        marriageStage = MarriageStage.WAITING_FOR_GIFT_SPIN;

        dashboard.refresh(List.of(player));

        if (player.isComputer()) {

            logPanel.log(player.getName() + " (AI) got married!", EVENT);

            // 🎯 DIRECT SPIN (no UI dependency)
            callback.spinForStop(spin -> handleSpin(spin));
        }
        else {

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

        logPanel.log("DAY OF RECKONING!", EVENT);

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

        logPanel.log(player.getName() + " chose Millionaire path", EVENT);

        currentStopPlayer = player;
        waitingForReckoningSpin = true;

        if (player.isComputer()) {
            callback.spinForStop(spin -> handleSpin(spin));
        } else {
            callback.enableSpin();
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

            logPanel.log(
                    player.getName()
                            + " chose number "
                            + chosenTycoonNumber,
                    INFO
            );

            waitingForTycoonSpin = true;

            callback.spinForStop(spin -> handleSpin(spin));

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

                callback.spinForStop(spin -> handleSpin(spin));
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
package org.gameoflife;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.List;
import java.util.Random;

public class StopSpaceHandler {

    public interface StopCallback {
        void enableSpin();
        void continueMovement(Player player, int steps);
        void endTurn();
    }

    private StopCallback callback;
    private PlayersDashboard dashboard;

    private enum MarriageStage {
        NONE,
        WAITING_FOR_GIFT_SPIN,
        WAITING_FOR_HONEYMOON_SPIN
    }

    private MarriageStage marriageStage = MarriageStage.NONE;
    private Player marriagePlayer;

    public StopSpaceHandler(PlayersDashboard dashboard, StopCallback callback) {
        this.dashboard = dashboard;
        this.callback = callback;
    }

    // =========================================================
    // 🎯 ENTRY
    // =========================================================

    public boolean isInProgress() {
        return marriageStage != MarriageStage.NONE;
    }

    public void handle(Player player, BoardSpace space) {

        String action = space.getAction();
        if (action == null) return;

        switch (action) {

            case "Marriage":
                handleMarriage(player);
                break;

            case "Reckoning":
                System.out.println("Reckoning logic coming soon...");
                break;
        }
    }

    // =========================================================
    // 🎯 MARRIAGE FLOW
    // =========================================================

    private void handleMarriage(Player player) {

        player.setMarried(true);

        marriagePlayer = player;
        marriageStage = MarriageStage.WAITING_FOR_GIFT_SPIN;

        Platform.runLater(() -> {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("💍 Marriage");
            alert.setHeaderText(player.getName() + " got married!");
            alert.setContentText("Spin again to collect your wedding gifts!");

            alert.showAndWait();

            callback.enableSpin();
        });

        dashboard.refresh(List.of(player));
    }

    public void handleSpin() {

        int spin = new Random().nextInt(10) + 1;
        System.out.println("Marriage Spin: " + spin);

        if (marriageStage == MarriageStage.WAITING_FOR_GIFT_SPIN) {

            int amountPerPlayer = getMarriageGiftAmount(spin);
            int totalGift = amountPerPlayer * 1; // simplify for now

            marriagePlayer.collect(totalGift);

            marriageStage = MarriageStage.WAITING_FOR_HONEYMOON_SPIN;

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("🎁 Wedding Gifts");
                alert.setHeaderText("You spun: " + spin);
                alert.setContentText("You received ₹" + totalGift +
                        "\n\nSpin again for your honeymoon!");
                alert.showAndWait();
            });

            dashboard.refresh(List.of(marriagePlayer));
        }

        else if (marriageStage == MarriageStage.WAITING_FOR_HONEYMOON_SPIN) {

            int honeymoonSteps = spin;

            marriageStage = MarriageStage.NONE;

            callback.continueMovement(marriagePlayer, honeymoonSteps);
        }
    }

    private int getMarriageGiftAmount(int spin) {

        if (spin >= 1 && spin <= 3) return 2000;
        if (spin <= 6) return 1000;

        return 0;
    }
}
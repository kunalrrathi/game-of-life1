package org.gameoflife;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.*;
import static utilities.LogColors.*;

public class WhiteSpaceHandler {

//    private Set<Integer> pendingWhiteSpaceIndexes = new LinkedHashSet<>();
//    private Queue<Runnable> whiteEventQueue = new LinkedList<>();

    private PlayersDashboard dashboard;
    private Board board;
    private GameEngine engine;
    private GameLogPanel logPanel;

    public enum InsuranceType {
        LIFE,
        AUTO,
        FIRE,
        STOCK
    }

    public WhiteSpaceHandler(Board board, PlayersDashboard dashboard, GameEngine engine, GameLogPanel logPanel) {
        this.board = board;
        this.dashboard = dashboard;
        this.engine = engine;
        this.logPanel = logPanel;
    }

    // =========================================================
    // 🎯 COLLECT WHITE SPACES
    // =========================================================

//    public void collect(BoardSpace space) {
//        pendingWhiteSpaceIndexes.add(space.getIndex());
//    }

    // =========================================================
    // 🎯 PROCESS WHITE SPACES
    // =========================================================

//    public void flush(Player player) {
//
//        if (pendingWhiteSpaceIndexes.isEmpty()) return;
//
//        System.out.println("Processing White Spaces...");
//
//        for (Integer index : pendingWhiteSpaceIndexes) {
//
//            BoardSpace space = board.getSpace(index);
//
//            whiteEventQueue.add(() -> handleWhite(player, space));
//        }
//
//        pendingWhiteSpaceIndexes.clear();
//
//        processQueue();
//    }

//    private void processQueue() {
//
//        if (whiteEventQueue.isEmpty()) return;
//
//        Runnable task = whiteEventQueue.poll();
//
//        Platform.runLater(() -> {
//            task.run();
//            processQueue();
//        });
//    }

    // =========================================================
    // 🎯 WHITE ACTION HANDLER
    // =========================================================

    public void handle(Player player, BoardSpace space) {

        String action = space.getAction();

        System.out.println("Resolving White Space Action: " + action);

        if (action != null) action = action.trim();

        int amount = space.getAmount();

        if (action == null) return;

        switch (action) {

            case "Pay-Life-Insurance":
                offerInsurance(player, InsuranceType.LIFE, amount);
                break;

            case "Pay-Auto-Insurance":
                offerInsurance(player, InsuranceType.AUTO, amount);
                break;

            case "Pay-Fire-Insurance":
                offerInsurance(player, InsuranceType.FIRE, amount);
                break;

            case "Pay-Stock-Insurance":
                offerInsurance(player, InsuranceType.STOCK, amount);
                break;

            case "Play-Market":
                handlePlayMarket(player);
                break;

            default:
                System.out.println("Unknown White action: " + action);
        }

        dashboard.refresh(List.of(player));
    }

    // =========================================================
    // 🎯 INSURANCE
    // =========================================================

    private void offerInsurance(Player player, InsuranceType type, int amount) {

        if (hasInsurance(player, type)) {
            System.out.println(player.getName() + " already has " + type + " insurance → skipping");
            return;
        }

        // 🧠 STEP 1: Handle COMPUTER PLAYER FIRST
        if (player.isComputer()) {

            ComputerDecisionEngine decisionEngine = engine.getDecisionEngine();

            boolean shouldBuy = decisionEngine.shouldBuyInsurance(player);

            if (shouldBuy) {
                player.pay(amount);
                setInsurance(player, type, true);

                logPanel.log(player.getName() + " (AI) bought " + type + " insurance", INFO);
            } else {
                logPanel.log(player.getName() + " (AI) skipped " + type + " insurance", INFO);
            }

            return; // 🚨 VERY IMPORTANT → prevents popup
        }

        // 👤 STEP 2: HUMAN PLAYER (existing logic untouched)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(type + " Insurance");
        alert.setHeaderText("Buy " + type + " Insurance?");
        alert.setContentText("Cost: ₹" + amount);

        ButtonType yes = new ButtonType("Buy");
        ButtonType no = new ButtonType("Skip");

        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yes) {
            player.pay(amount);
            setInsurance(player, type, true);

            logPanel.log(player.getName() + " bought " + type + " insurance", INFO);
        }
    }

    private boolean hasInsurance(Player player, InsuranceType type) {

        switch (type) {
            case LIFE: return player.isLifeInsurance();
            case AUTO: return player.hasAutoInsurance();
            case FIRE: return player.hasFireInsurance();
            case STOCK: return player.hasStockInsurance();
        }
        return false;
    }

    private void setInsurance(Player player, InsuranceType type, boolean value) {

        switch (type) {
            case LIFE: player.setLifeInsurance(value); break;
            case AUTO: player.setAutoInsurance(value); break;
            case FIRE: player.setFireInsurance(value); break;
            case STOCK:
                player.setStockInsurance(value);
                player.setHasStock(true);
                break;
        }
    }

    // =========================================================
    // 🎯 MARKET
    // =========================================================

    private void handlePlayMarket(Player player) {

        if (!player.hasStock()) {
            logPanel.log(player.getName() + " has no stock → cannot play market", INFO);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("📈 Play the Market");
        alert.setHeaderText("Do you want to play the market?");
        alert.setContentText("Risky move! Spin will decide your fate.");

        ButtonType play = new ButtonType("Play");
        ButtonType skip = new ButtonType("Skip");

        alert.getButtonTypes().setAll(play, skip);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == play) {

            int spin = new Random().nextInt(10) + 1;
            logPanel.log("Market Spin: " + spin, EVENT);

            if (spin <= 3) {
                player.pay(60000);
                showResult("📉 Market Down", "You lost ₹60000");
            } else if (spin <= 6) {
                showResult("😐 Market Stable", "No gain, no loss");
            } else {
                player.collect(120000);
                showResult("📈 Market Up", "You gained ₹120000!");
            }
        }
    }

    private void showResult(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Market Result");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
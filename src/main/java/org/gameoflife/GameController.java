package org.gameoflife;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.*;

public class GameController {

    private BorderPane root;

    private Board board;
    private List<Player> players;
    private List<PlayerToken> tokens = new ArrayList<>();

    private PlayersDashboard dashboard;
    private GameEngine engine;

    private Button spinButton;
    private BoardSpace space;

    private boolean stopMovement = false; // Flag to control movement stopping

    private Set<Integer> pendingWhiteSpaceIndexes = new LinkedHashSet<>();

    private Queue<Runnable> whiteEventQueue = new LinkedList<>();

    private enum MarriageStage {
        NONE,
        WAITING_FOR_GIFT_SPIN,
        WAITING_FOR_HONEYMOON_SPIN
    }

    public enum InsuranceType {
        LIFE,
        AUTO,
        FIRE,
        STOCK
    }

    private MarriageStage marriageStage = MarriageStage.NONE;
    private Player marriagePlayer;
    private int lastGiftAmount;

    public GameController() {

        root = new BorderPane();

        // 🟢 Step 1: Setup dialog
        players = GameSetupDialog.showDialog();

        // 🟢 Step 2: Board
        board = new Board();

        // 🟢 Step 3: Dashboard
        dashboard = new PlayersDashboard();

        for (Player p : players) {
            dashboard.addPlayer(p.getName());
        }

        // 🟢 Step 4: Tokens
        createTokens();

        // 🟢 Step 5: Engine
        engine = new GameEngine(board, players, tokens);

        // 🟢 Step 6: Controls
        setupControls();

        // 🟢 Step 7: Layout
        root.setCenter(board.getBoardPane());
        root.setRight(dashboard.getPanel());
        root.setBottom(new HBox(10, spinButton));
    }

    // =========================================================
    // 🎯 TOKEN CREATION
    // =========================================================

    private void createTokens() {

        // Predefined colors
        javafx.scene.paint.Color[] colors = {
                javafx.scene.paint.Color.BLUE,
                javafx.scene.paint.Color.RED,
                javafx.scene.paint.Color.GREEN,
                javafx.scene.paint.Color.YELLOW,
                javafx.scene.paint.Color.PURPLE,
                javafx.scene.paint.Color.ORANGE
        };

        for (int i = 0; i < players.size(); i++) {

            PlayerToken token = new PlayerToken(players.get(i), colors[i]);

            tokens.add(token);
            board.addToken(token);
        }

        // Place all tokens at START (index 0)
        board.positionTokens(tokens, 0);
    }

    // =========================================================
    // 🎯 CONTROLS (SPIN BUTTON)
    // =========================================================

    private void setupControls() {

        spinButton = new Button("Spin");

        spinButton.setOnAction(e -> handleSpinClick());
    }

    private void handleSpinClick() {

        // 🎯 Marriage flow override
        if (marriageStage != MarriageStage.NONE) {
            handleMarriageSpin();
            return;
        }

        Player player = engine.getCurrentPlayer();
        PlayerToken token = engine.getCurrentToken();

        int steps = new Random().nextInt(10) + 1;
        System.out.println(player.getName() + " Spun: " + steps);

        spinButton.setDisable(true);

        startMovement(player, token, steps);
    }

    private void startMovement(Player player, PlayerToken token, int steps) {

        board.animateMovement(
                token,
                steps,

                // 🔁 STEP CALLBACK
                (space, remainingSteps) -> handleStep(space, remainingSteps),

                // 🎯 FINAL LANDING
                () -> handleLanding(token)
        );
    }

    private void handleStep(BoardSpace space, int remainingSteps) {

        Player player = engine.getCurrentPlayer();

        // 🚨 STOP (highest priority)
        if ("Stop".equalsIgnoreCase(space.getColor())) {

            System.out.println("STOP encountered → forcing stop");

            board.stopAnimation();

            // 🎯 NEW: process white spaces BEFORE STOP logic
            flushPendingEvents(player);

            processStep(player, space, true);

            if (marriageStage == MarriageStage.NONE) {
                endTurn();
            }

            return;
        }

        // 🔁 PASS logic
        processStep(player, space, false);

        // 🔀 Split handling
        if ("Split".equalsIgnoreCase(space.getSpaceType())) {

            board.stopAnimation();

            Platform.runLater(() ->
                    handleSplit(space, remainingSteps)
            );
        }
    }

    private void handleLanding(PlayerToken token) {

        Player player = engine.getCurrentPlayer();

        BoardSpace landed = board.getSpace(token.getCurrentIndex());

        processStep(player, landed, true);

        // 🎯 NEW: process all pending white spaces
        flushPendingEvents(player);

        endTurn();
    }

    private void endTurn() {
        engine.nextTurn();
        spinButton.setDisable(false);
    }

    private void continueMovementAfterStop(Player player, int steps) {

        PlayerToken token = engine.getCurrentToken();

        // Reset stop flag BEFORE movement resumes
        stopMovement = false;

        board.animateMovement(
                token,
                steps,

                // STEP CALLBACK
                (space, remainingSteps) -> {

                    // 🚨 STOP again (important for chained stops)
                    if ("Stop".equalsIgnoreCase(space.getColor())) {

                        board.stopAnimation();

                        // 🎯 NEW
                        flushPendingEvents(player);

                        processStep(player, space, true);

                        if (marriageStage == MarriageStage.NONE) {
                            engine.nextTurn();
                            spinButton.setDisable(false);
                        }

                        return;
                    }

                    processStep(player, space, false);

                    if (stopMovement) {
                        board.stopAnimation();
                        return;
                    }
                },

                // FINAL LANDING
                () -> {
                    BoardSpace landed = board.getSpace(token.getCurrentIndex());

                    processStep(player, landed, true);

                    // 🎯 NEW
                    flushPendingEvents(player);

                    engine.nextTurn();
                    spinButton.setDisable(false);
                }
        );
    }

    private void handleSplit(BoardSpace space, int remainingSteps) {

        System.out.println("Split space reached! Offering path choices...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Choose Path");
        alert.setHeaderText("Select your Career Path");
        alert.setContentText("Business Route: Faster but less salary\nUniversity Route: Slower but multiple career options");

        ButtonType business = new ButtonType("Business Route");
        ButtonType university = new ButtonType("University Route");

        alert.getButtonTypes().setAll(business, university);

        alert.showAndWait().ifPresent(choice -> {

            int nextIndex;

            if (choice == university) {
                nextIndex = findNextByType("Main", space.getIndex());
            } else {
                nextIndex = findNextByType("Shortcut", space.getIndex());
            }

            PlayerToken token = engine.getCurrentToken();
            Player currentPlayer = engine.getCurrentPlayer();

            token.setCurrentIndex(nextIndex);

            BoardSpace next = board.getSpace(nextIndex);

            token.getNode().setLayoutX(next.getX());
            token.getNode().setLayoutY(next.getY());

            // 🔥 Resume movement
            if (remainingSteps > 0) {

                board.animateMovement(
                        token,
                        remainingSteps,

                        // 🔁 STEP CALLBACK (PASS LOGIC ADDED)
                        (nextSpace, nextRemainingSteps) -> {

                            processStep(currentPlayer, nextSpace, false);

                            if ("Split".equalsIgnoreCase(nextSpace.getSpaceType())) {

                                board.stopAnimation();

                                Platform.runLater(() ->
                                        handleSplit(nextSpace, nextRemainingSteps)
                                );
                            }
                        },

                        // ✅ FINAL LANDING (UPDATED)
                        () -> {
                            BoardSpace landed = board.getSpace(token.getCurrentIndex());

                            processStep(currentPlayer, landed, true);

                            // 🔥 CRITICAL FIX
                            flushPendingEvents(currentPlayer);

                            engine.nextTurn();
                            spinButton.setDisable(false);
                        }
                );

            } else {
//                currentPlayer = engine.getCurrentPlayer();

                BoardSpace landed = board.getSpace(token.getCurrentIndex());

                // ✅ CRITICAL FIX → process landing
                processStep(currentPlayer, landed, true);

                engine.nextTurn();
                spinButton.setDisable(false);
            }
        });
    }

    private int findNextByType(String type, int fromIndex) {

        for (int i = fromIndex + 1; i < 200; i++) {

            space = board.getSpace(i);

            if (type.equalsIgnoreCase(space.getSpaceType())) {
                return space.getIndex();
            }
        }

        return fromIndex + 1;
    }

    private void processStep(Player player, BoardSpace space, boolean isLanding) {

        String color = space.getColor();
        String action = space.getAction();
        if (action != null) action = action.trim();

        if (color == null) color = "";

        // 🔴 RED → PASS + LAND
        if ("Red".equalsIgnoreCase(color)) {
            System.out.println("Red space: " + (isLanding ? "Landing" : "Passing") + " - Action: " + action);
            handleRed(player, space);
        }

        // ⚪ WHITE → LAND ONLY (later)
        else if ("White".equalsIgnoreCase(color)) {
            System.out.println("White space: " + (isLanding ? "Landing" : "Passing") + " - Action: " + action);
            pendingWhiteSpaceIndexes.add(space.getIndex());
        }

        // 🟡 JUMP → handled later
        else if ("Jump".equalsIgnoreCase(color)) {
            System.out.println("Jump space: " + (isLanding ? "Landing" : "Passing") + " - Action: " + action);
            if (!isLanding) return;
            handleJump(player, space);
            return;
        }

        // 🛑 STOP → handled later
        else if ("Stop".equalsIgnoreCase(color)) {

            System.out.println("Stop space: " + (isLanding ? "Landing" : "Passing") + " - Action: " + action);
            if (!isLanding) return;

            handleStop(player, space);
            return;
        }

        // ⚪ NORMAL (no color) → LAND ONLY
        else if (isLanding) {
            System.out.println("Normal space: Landing - Action: " + action);
            handleNormal(player, space);
        }
    }

    private void handleRed(Player player, BoardSpace space) {

        String action = space.getAction();

        if (action == null) return;

        switch (action) {

            case "PayDay":
                player.collect(player.getSalary());
                break;

            case "Collect":
                player.collect(space.getAmount());
                break;

            case "Pay":
                player.pay(space.getAmount());
                break;

            case "Wait-Turn":
//                player.setSkipNextTurn(true);
                break;

            case "Business":
                handleBusiness(player, space);
                break;

            case "Accident":
                handleClaim(player, InsuranceType.AUTO, space.getAmount(), "🚗 Accident");
                break;

            case "Fire":
                handleClaim(player, InsuranceType.FIRE, space.getAmount(), "🔥 Fire");
                break;

            case "Stock-Crash":
                handleClaim(player, InsuranceType.STOCK, space.getAmount(), "📉 Stock Crash");
                break;
        }

        dashboard.refresh(players);
    }

    private void handleBusiness(Player player, BoardSpace space) {
        if (player.getProfession() == Profession.NONE) {
            player.setProfession(Profession.BUSINESS);
            player.addCash(Profession.BUSINESS.getSalary()); // initial boost
            System.out.println(player.getName() + " profession is set to as " + player.getProfession() + " with salary " + player.getSalary());
        } else
            System.out.println(player.getName() + " already has a profession → " + player.getProfession());
    }

    private void processWhiteSpaces(Player player) {

        if (pendingWhiteSpaceIndexes.isEmpty()) return;

        System.out.println("Processing White Spaces...");

        for (Integer index : pendingWhiteSpaceIndexes) {

            BoardSpace space = board.getSpace(index);

            whiteEventQueue.add(() -> handleWhite(player, space));
        }

        pendingWhiteSpaceIndexes.clear();

        // 🎯 Start processing queue AFTER animation
        processWhiteEventQueue();
    }

    private void processWhiteEventQueue() {

        if (whiteEventQueue.isEmpty()) return;

        Runnable task = whiteEventQueue.poll();

        Platform.runLater(() -> {

            task.run(); // showAndWait happens safely here

            // Process next AFTER this finishes
            processWhiteEventQueue();
        });
    }

    private void flushPendingEvents(Player player) {
        processWhiteSpaces(player);
    }

    private void handleWhite(Player player, BoardSpace space) {

        String action = space.getAction();
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

        dashboard.refresh(players);
    }

    private void offerInsurance(Player player, InsuranceType type, int amount) {

        // 🛡 Already owned check
        if (hasInsurance(player, type)) {
            System.out.println(player.getName() + " already has " + type + " insurance → skipping");
            return;
        }

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

            System.out.println(player.getName() + " bought " + type + " insurance");
        }

        dashboard.refresh(players);
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
            case STOCK: player.setStockInsurance(value); player.setHasStock(true); break;
        }
    }

    private void handleClaim(Player player,
                             InsuranceType type,
                             int penaltyAmount,
                             String eventName) {

        boolean insured = hasInsurance(player, type);

        if (insured) {

            System.out.println(player.getName() + " is insured for " + type);

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Insurance Claim");
                alert.setHeaderText(eventName);
                alert.setContentText("You are insured! No payment needed 🎉");
                alert.showAndWait();
            });

        } else {

            System.out.println(player.getName() + " is NOT insured for " + type);

            player.pay(penaltyAmount);

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Insurance");
                alert.setHeaderText(eventName);
                alert.setContentText("You paid ₹" + penaltyAmount);
                alert.showAndWait();
            });
        }

        dashboard.refresh(players);
    }

    private void handlePlayMarket(Player player) {

        // 🛡 Must own stock
        if (!player.hasStock()) {
            System.out.println(player.getName() + " has no stock → cannot play market");
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

            int spin = spinWheel();

            System.out.println("Market Spin: " + spin);

            if (spin >= 1 && spin <= 3) {

                player.pay(60000);

                showMarketResult("📉 Market Down",
                        "You lost ₹60000");

            } else if (spin >= 4 && spin <= 6) {

                showMarketResult("😐 Market Stable",
                        "No gain, no loss");

            } else {

                player.collect(120000);

                showMarketResult("📈 Market Up",
                        "You gained ₹120000!");
            }

            dashboard.refresh(players);
        }
    }

    private void showMarketResult(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Market Result");
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void handleJump(Player player, BoardSpace space) {

        String action = space.getAction();

        if (action == null) return;

//        // 🚫 Prevent re-assigning profession (important safety)
//        if (player.getProfession() != Profession.NONE) {
//            System.out.println(player.getName() + " already has a profession: " + player.getProfession());
//            return;
//        }

        if ("Go-To-Start".equalsIgnoreCase(action)) {

            moveToTarget(player, space);
            return;
        }

        // 🎯 Map action → Profession
        Profession profession = mapToProfession(action);

        if (profession != null) {

            player.setProfession(profession);
            player.addCash(profession.getSalary()); // Give initial salary boost

            System.out.println(player.getName() + " chose career: " + profession +
                    " | Salary: " + profession.getSalary());

            // 🎉 SHOW POPUP HERE
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Career Selected");
                alert.setHeaderText(null);
                alert.setContentText(player.getName() + " is now a " + profession +
                        "\nSalary: " + profession.getSalary());
                alert.showAndWait();
            });
        }

        // 🎯 Move player + token to target (VERY IMPORTANT)
        Integer targetIndex = space.getBranch();

        if (targetIndex != null) {

            // Update player model
            player.setPosition(targetIndex);

            // Update token UI
            PlayerToken token = engine.getCurrentToken();
            token.setCurrentIndex(targetIndex);

            BoardSpace targetSpace = board.getSpace(targetIndex);

            token.getNode().setLayoutX(targetSpace.getX());
            token.getNode().setLayoutY(targetSpace.getY());
        }

        // 🔄 Refresh UI
        dashboard.refresh(players);
    }

    private Profession mapToProfession(String action) {

        switch (action) {

            case "Doctor":
                return Profession.DOCTOR;

            case "Lawyer":
                return Profession.LAWYER;

            case "Journalist":
                return Profession.JOURNALIST;

            case "Teacher":
                return Profession.TEACHER;

            case "Physicist":
                return Profession.PHYSICIST;

            case "University":
                return Profession.UNIVERSITY_DEGREE;

            case "Business":
                return Profession.BUSINESS;

            default:
                return null;
        }
    }

    private void moveToTarget(Player player, BoardSpace space) {

        Integer targetIndex = space.getBranch();

        if (targetIndex == null) return;

        player.setPosition(targetIndex);

        PlayerToken token = engine.getCurrentToken();
        token.setCurrentIndex(targetIndex);

        BoardSpace target = board.getSpace(targetIndex);

        token.getNode().setLayoutX(target.getX());
        token.getNode().setLayoutY(target.getY());
    }

    private void handleStop(Player player, BoardSpace space) {

        String action = space.getAction();

        if (action == null) return;

        // 🚨 Stop further movement
        stopMovement = true;

        switch (action) {

            case "Marriage":
                handleMarriage(player);
                break;

            case "Reckoning":
                System.out.println("Reckoning logic coming soon...");
                break;
        }
    }

    private void handleMarriage(Player player) {

        stopMovement = true;

        player.setMarried(true);
        marriagePlayer = player;
        marriageStage = MarriageStage.WAITING_FOR_GIFT_SPIN;

        Platform.runLater(() -> {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("💍 Marriage");
            alert.setHeaderText(player.getName() + " got married!");
            alert.setContentText("Spin again to collect your wedding gifts!");

            alert.showAndWait();

            // Enable spin button for next step
            spinButton.setDisable(false);
        });

        dashboard.refresh(players);
    }

    private void handleMarriageSpin() {

        int spin = new Random().nextInt(10) + 1;
        System.out.println("Marriage Spin: " + spin);

        if (marriageStage == MarriageStage.WAITING_FOR_GIFT_SPIN) {

            int amountPerPlayer = getMarriageGiftAmount(spin);
            int totalGift = amountPerPlayer * (players.size() - 1);

            marriagePlayer.collect(totalGift);
            lastGiftAmount = totalGift;

            marriageStage = MarriageStage.WAITING_FOR_HONEYMOON_SPIN;

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("🎁 Wedding Gifts");
                alert.setHeaderText("You spun: " + spin);
                alert.setContentText("You received ₹" + totalGift +
                        "\n\nSpin again for your honeymoon!");

                alert.showAndWait();
            });

            dashboard.refresh(players);
        }

        else if (marriageStage == MarriageStage.WAITING_FOR_HONEYMOON_SPIN) {

            int honeymoonSteps = spin;

            marriageStage = MarriageStage.NONE;

            spinButton.setDisable(true); // disable during movement

            Platform.runLater(() -> {
                continueMovementAfterStop(marriagePlayer, honeymoonSteps);
            });
        }
    }

    private int spinWheel() {
        int newSpin = new Random().nextInt(10) + 1;
        System.out.println("Spin result: " + newSpin);
        return newSpin;
    }

    private int getMarriageGiftAmount(int spin) {

        if (spin >= 1 && spin <= 3) return 2000;
        if (spin >= 4 && spin <= 6) return 1000;

        return 0;
    }



    private void handleNormal(Player player, BoardSpace space) {

        String action = space.getAction();

        if (action == null) return;

        switch (action) {

            case "Collect":
                player.collect(space.getAmount());
                break;

            case "Pay":
                player.pay(space.getAmount());
                break;

            case "PayDay":
                player.collect(player.getSalary());
                System.out.println("PayDay! Collected salary: " + player.getSalary());
                break;

            case "Spin-Again":
                return;

            case "Wait-Turn":
                break;

            case "Business":
            case "University":
                System.out.println("Future logic");
                break;
        }

        dashboard.refresh(players);
    }



    // =========================================================
    // 🎯 ROOT
    // =========================================================

    public BorderPane getRoot() {
        return root;
    }
}
package org.gameoflife;

import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

//import java.applet.AudioClip;
import java.util.*;

import static utilities.SpinSoundGenerator.playSpinSound;

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

    private WhiteSpaceHandler whiteHandler;
    private RedSpaceHandler redHandler;
    private JumpSpaceHandler jumpHandler;
    private StopSpaceHandler stopHandler;

    private StackPane spinnerContainer;

    public enum InsuranceType {
        LIFE,
        AUTO,
        FIRE,
        STOCK
    }

    public GameController() {

        root = new BorderPane();


        // 🟢 Step 1: Setup dialog
        players = GameSetupDialog.showDialog();

        // 🟢 Step 2: Board
        board = new Board();
        this.spinnerContainer = board.getSpinnerContainer();

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

        // 🟢 Step 7: Initialize Handlers
        whiteHandler = new WhiteSpaceHandler(board, dashboard, engine);
        redHandler = new RedSpaceHandler(dashboard);
        jumpHandler = new JumpSpaceHandler(board, engine, dashboard);
        stopHandler = new StopSpaceHandler(dashboard, new StopSpaceHandler.StopCallback() {

            @Override
            public void enableSpin() {
                spinButton.setDisable(false);
            }

            @Override
            public void continueMovement(Player player, int steps) {
                continueMovementAfterStop(player, steps);
            }

            @Override
            public void endTurn() {
                engine.nextTurn();

                Platform.runLater(() -> {
                    if (!checkGameEnd()) {
                        triggerNextTurn(); // 🚀 handles both AI + human
                    }
                });
                spinButton.setDisable(false);
            }

            @Override
            public void endGame(Player winner) {
                showWinner(winner);
                spinButton.setDisable(true);
            }
        });

        // 🟢 Step 8: Layout
        root.setCenter(board.getBoardPane());
        root.setRight(dashboard.getPanel());
//        root.setBottom(new HBox(10, spinButton));
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
        spinnerContainer.setOnMouseClicked(e -> handleSpinClick());
    }

    private void handleSpinClick() {

        // 🎯 Marriage flow override
        if (stopHandler.isInProgress()) {
            stopHandler.handleSpin();
            return;
        }

        Player player = engine.getCurrentPlayer();
        PlayerToken token = engine.getCurrentToken();

        int steps = new Random().nextInt(10) + 1;

        System.out.println(player.getName() + " Spinning...");

        spinnerContainer.setDisable(true); // prevent double clicks

        double anglePerStep = 360.0 / 10;
        double wheelAngle = (steps - 1) * anglePerStep;

        double arrowOffset = 180;
        double baseOffset = 335; // tweak this

        double targetAngle = 360 * 4 + arrowOffset + wheelAngle - baseOffset;

        System.out.println("Steps: " + steps + " | Angle: " + targetAngle);

        // 🎯 Animation
        RotateTransition rotate = new RotateTransition(Duration.seconds(2), spinnerContainer);
        rotate.setToAngle(targetAngle);
        rotate.setInterpolator(Interpolator.EASE_OUT);

        // 🔊 Play sound
        playSpinSound();

        rotate.setOnFinished(e -> {

            System.out.println(player.getName() + " Spun: " + steps);

            spinnerContainer.setDisable(false);

            // 🚀 Continue game logic
            startMovement(player, token, steps);
        });

        rotate.play();
    }

//    private void playSpinSoundBKUP() {
//
//        AudioClip clip = new AudioClip(
//                getClass().getResource("/spin.mp3").toExternalForm()
//        );
//
//        clip.play();
//    }

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

            if (stopHandler.isInProgress()) {
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

    private void triggerNextTurn() {

        Player currentPlayer = engine.getCurrentPlayer();

        if (currentPlayer.isComputer()) {

            System.out.println(currentPlayer.getName() + " (AI) is taking turn...");

            spinButton.setDisable(true); // prevent manual click

            PauseTransition delay = new PauseTransition(Duration.seconds(1.2));

            delay.setOnFinished(e -> {
                spinButton.setDisable(false); // 🔥 CRITICAL FIX
                spinButton.setDisable(false); // 🔥 CRITICAL FIX
                Platform.runLater(() -> handleSpinClick());            // AI Turn Logic
            });

            delay.play();

        } else {
            spinButton.setDisable(false); // human plays
        }
    }

    private void endTurn() {
        engine.nextTurn();

        Platform.runLater(() -> {
            if (!checkGameEnd()) {
                triggerNextTurn(); // 🚀 handles both AI + human
            }
        });
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

                        if (!stopHandler.isInProgress()) {
                            engine.nextTurn();

                            Platform.runLater(() -> {
                                if (!checkGameEnd()) {
                                    triggerNextTurn(); // 🚀 handles both AI + human
                                }
                            });
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

                    Platform.runLater(() -> {
                        if (!checkGameEnd()) {
                            triggerNextTurn(); // 🚀 handles both AI + human
                        }
                    });
                    spinButton.setDisable(false);
                }
        );
    }

    private void handleSplit(BoardSpace space, int remainingSteps) {

        System.out.println("Split space reached! Offering path choices...");

        PlayerToken token = engine.getCurrentToken();
        Player currentPlayer = engine.getCurrentPlayer();

        // 🧠 Decide path FIRST
        int nextIndex;

        if (currentPlayer.isComputer()) {

            // 🤖 AI Decision
            ComputerDecisionEngine decisionEngine = engine.getDecisionEngine();

            boolean chooseUniversity = decisionEngine.chooseUniversityPath(currentPlayer);

            if (chooseUniversity) {
                nextIndex = findNextByType("Main", space.getIndex());
                System.out.println(currentPlayer.getName() + " (AI) chose University Route");
            } else {
                nextIndex = findNextByType("Shortcut", space.getIndex());
                System.out.println(currentPlayer.getName() + " (AI) chose Business Route");
            }

            // 👉 Continue flow directly
            continueMovementAfterSplit(token, currentPlayer, nextIndex, remainingSteps, space);

        } else {

            // 👤 HUMAN FLOW (existing popup)
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Choose Path");
            alert.setHeaderText("Select your Career Path");
            alert.setContentText("Business Route: Faster but less salary\nUniversity Route: Slower but multiple career options");

            ButtonType business = new ButtonType("Business Route");
            ButtonType university = new ButtonType("University Route");

            alert.getButtonTypes().setAll(business, university);

            alert.showAndWait().ifPresent(choice -> {

                int selectedIndex;

                if (choice == university) {
                    selectedIndex = findNextByType("Main", space.getIndex());
                } else {
                    selectedIndex = findNextByType("Shortcut", space.getIndex());
                }

                continueMovementAfterSplit(token, currentPlayer, selectedIndex, remainingSteps, space);
            });
        }
    }

    private void continueMovementAfterSplit(
            PlayerToken token,
            Player currentPlayer,
            int nextIndex,
            int remainingSteps,
            BoardSpace space
    ) {

        token.setCurrentIndex(nextIndex);

        BoardSpace next = board.getSpace(nextIndex);

        token.getNode().setLayoutX(next.getX());
        token.getNode().setLayoutY(next.getY());

        if (remainingSteps > 0) {

            board.animateMovement(
                    token,
                    remainingSteps,

                    // 🔁 STEP CALLBACK
                    (nextSpace, nextRemainingSteps) -> {

                        processStep(currentPlayer, nextSpace, false);

                        if ("Split".equalsIgnoreCase(nextSpace.getSpaceType())) {

                            board.stopAnimation();

                            Platform.runLater(() ->
                                    handleSplit(nextSpace, nextRemainingSteps)
                            );
                        }
                    },

                    // ✅ FINAL LANDING
                    () -> {
                        BoardSpace landed = board.getSpace(token.getCurrentIndex());

                        processStep(currentPlayer, landed, true);

                        flushPendingEvents(currentPlayer);

                        engine.nextTurn();

                        Platform.runLater(() -> {
                            if (!checkGameEnd()) {
                                triggerNextTurn(); // 🚀 handles both AI + human
                            }
                        });
                        spinButton.setDisable(false);
                    }
            );

        } else {

            BoardSpace landed = board.getSpace(token.getCurrentIndex());

            processStep(currentPlayer, landed, true);

            engine.nextTurn();

            Platform.runLater(() -> {
                if (!checkGameEnd()) {
                    triggerNextTurn(); // 🚀 handles both AI + human
                }
            });
            spinButton.setDisable(false);
        }
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
            redHandler.handle(player, space);

            // 👉 Keep claim logic here (important)
            if ("Accident".equalsIgnoreCase(action)) {
                handleClaim(player, InsuranceType.AUTO, space.getAmount(), "🚗 Accident");
            }
            else if ("Fire".equalsIgnoreCase(action)) {
                handleClaim(player, InsuranceType.FIRE, space.getAmount(), "🔥 Fire");
            }
            else if ("Stock-Crash".equalsIgnoreCase(action)) {
                handleClaim(player, InsuranceType.STOCK, space.getAmount(), "📉 Stock Crash");
            }
        }

        // ⚪ WHITE → LAND ONLY (later)
        else if ("White".equalsIgnoreCase(color)) {
            System.out.println("White space: " + (isLanding ? "Landing" : "Passing") + " - Action: " + action);
            whiteHandler.collect(space);
        }

        // 🟡 JUMP → handled later
        else if ("Jump".equalsIgnoreCase(color)) {
            System.out.println("Jump space: " + (isLanding ? "Landing" : "Passing") + " - Action: " + action);
            if (!isLanding) return;
            jumpHandler.handle(player, space);
            return;
        }

        // 🛑 STOP → handled later
        else if ("Stop".equalsIgnoreCase(color)) {
            System.out.println("Stop space: " + (isLanding ? "Landing" : "Passing") + " - Action: " + action);
            if (!isLanding) return;
            stopHandler.handle(player, space);
            return;
        }

        // ⚪ NORMAL (no color) → LAND ONLY
        else if (isLanding) {
            System.out.println("Normal space: Landing - Action: " + action);
            handleNormal(player, space);
        }

        //Retirement logic
        else if ("Retire".equalsIgnoreCase(action)) {
            handleRetirement(player);
            return;
        }
    }

    private void flushPendingEvents(Player player) {
        whiteHandler.flush(player);
    }

    private void handleClaim(Player player,
                             InsuranceType type,
                             int penaltyAmount,
                             String eventName) {

        boolean insured = player.hasInsurance(type);

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

    private void handleRetirement(Player player) {

        if (player.isRetired()) return; // 🔥 IMPORTANT

        System.out.println(player.getName() + " reached MILLIONAIRE!");

        // 🏆 First player bonus
        if (!engine.hasMillionaire()) {

            engine.setFirstMillionaire(player);

            player.collect(240000);

            int lucky = spinWheel();

            engine.setLuckyNumber(lucky);

            if(!player.isComputer())
                showPopup("🏆 First Millionaire!",
                        player.getName() + " gets ₹240000 bonus!\nLucky Number: " + lucky);

        } else {
            if (!player.isComputer())
                showPopup("🎉 Millionaire",
                        player.getName() + " reached Millionaire!");
        }
        player.setRetired(true);
        dashboard.refresh(players);
    }

    private void applyLuckyNumberRule(Player currentPlayer, int spin) {

        Player millionaire = engine.getFirstMillionaire();
        Integer lucky = engine.getLuckyNumber();

        if (millionaire == null || lucky == null) return;

        // Do not apply to the millionaire themselves
        if (currentPlayer == millionaire) return;

        if (spin == lucky) {

            currentPlayer.pay(24000);
            millionaire.collect(24000);
            if (!currentPlayer.isComputer())
                showPopup("🎯 Lucky Number!",
                        currentPlayer.getName() + " hit " + lucky +
                                "\nPaid ₹24000 to " + millionaire.getName());
        }
    }

    private void showPopup(String title, String message) {

        Platform.runLater(() -> {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Update");
            alert.setHeaderText(title);
            alert.setContentText(message);

            alert.showAndWait();
        });
    }

    private int spinWheel() {
        int newSpin = new Random().nextInt(10) + 1;
        System.out.println("Spin result: " + newSpin);
        return newSpin;
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
        }

        dashboard.refresh(players);
    }

    private boolean checkGameEnd() {

        boolean allFinished = players.stream()
                .allMatch(p -> p.isBankrupt() || p.isRetired());

        if (!allFinished) {
            return false; // ⛔ game still running
        }

        Player winner = players.stream()
                .max(Comparator.comparingInt(this::calculateFinalWealth))
                .orElse(null);

        showWinner(winner);

        // 🔥 Existing logic
        updateGameControls();

        return true; // ✅ game ended
    }

    private int calculateFinalWealth(Player player) {

        int total = player.getCash();

        // 💰 Stock value
        if (player.hasStock()) {
            total += 120000;
        }

        // 💰 Life insurance value
        if (player.isLifeInsurance()) {
            total += 8000;
        }

        return total;
    }

    private void showWinner(Player winner) {

        if (winner == null) return;

        Platform.runLater(() -> {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("🏁 Game Over");
            alert.setHeaderText("Winner: " + winner.getName());
            alert.setContentText("Total Wealth: ₹" + calculateFinalWealth(winner));

            alert.showAndWait();
        });
    }


    private void updateGameControls() {

        boolean allFinished = players.stream()
                .allMatch(p -> p.isRetired() || p.isBankrupt());

        if (allFinished) {
            spinButton.setDisable(true);
        }
    }

    private void handleComputerTurn(Player player) {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));

        delay.setOnFinished(e -> {
            spinWheel(); // reuse your existing spin logic
        });

        delay.play();
    }

    // =========================================================
    // 🎯 ROOT
    // =========================================================

    public BorderPane getRoot() {
        return root;
    }
}
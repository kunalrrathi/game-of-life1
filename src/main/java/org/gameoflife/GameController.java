package org.gameoflife;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private enum MarriageStage {
        NONE,
        WAITING_FOR_GIFT_SPIN,
        WAITING_FOR_HONEYMOON_SPIN
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

        spinButton.setOnAction(e -> {

            // 🎯 HANDLE MARRIAGE FLOW FIRST
            if (marriageStage != MarriageStage.NONE) {
                handleMarriageSpin();
                return;
            }
            Player currentPlayer = engine.getCurrentPlayer();

            int steps = new Random().nextInt(10) + 1;
            System.out.println(currentPlayer.getName() + " Spun: " + steps);


            PlayerToken token = engine.getCurrentToken();

            spinButton.setDisable(true);

            board.animateMovement(
                    token,
                    steps,
                    (space, remainingSteps) -> {

                        Player player = engine.getCurrentPlayer();

                        // 🚨 STOP SPACE (HIGHEST PRIORITY)
                        if ("Stop".equalsIgnoreCase(space.getColor())) {

                            System.out.println("STOP encountered → forcing stop");

                            board.stopAnimation();

                            // ✅ Treat as landing
                            processStep(player, space, true);

                            // DO NOT call nextTurn here if Marriage flow is active
                            if (marriageStage == MarriageStage.NONE) {
                                engine.nextTurn();
                                spinButton.setDisable(false);
                            }

                            return;
                        }

                        // ✅ Normal PASS processing
                        processStep(player, space, false);

                        // 🔀 Split logic (unchanged)
                        if ("Split".equalsIgnoreCase(space.getSpaceType())) {

                            board.stopAnimation();

                            Platform.runLater(() ->
                                    handleSplit(space, remainingSteps)
                            );
                        }
                    },
                    () -> {
                        BoardSpace landed = board.getSpace(token.getCurrentIndex());
                        processStep(currentPlayer, landed, true);
                        engine.nextTurn();
                        spinButton.setDisable(false);
                    }
            );
        });
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

                        System.out.println("STOP encountered during honeymoon");

                        board.stopAnimation();

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

                    engine.nextTurn();
                    spinButton.setDisable(false);
                }
        );
    }

    private void handleSplit(BoardSpace space, int remainingSteps) {

        System.out.println("Split space reached! Offering path choices...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Choose Path");
        alert.setHeaderText("Select your route");

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

        if (color == null) color = "";

        // 🔴 RED → PASS + LAND
        if ("Red".equalsIgnoreCase(color)) {
            System.out.println("Red space: " + (isLanding ? "Landing" : "Passing") + " - Action: " + action);
            handleRed(player, space);
        }

        // ⚪ WHITE → LAND ONLY (later)
        else if ("White".equalsIgnoreCase(color) && isLanding) {
            System.out.println("White space: Landing - Action: " + action);
//            handleWhite(player, space);
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
                System.out.println("Business logic later");
                break;
        }

        dashboard.refresh(players);
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
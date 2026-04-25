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

    private SpinnerController spinnerController;
    private TurnManager turnManager;
    private MovementController movementController;
    private SpaceResolver spaceResolver;

    private Player instantWinner;
    private boolean tycoonWin = false;

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
        spinnerController = new SpinnerController(spinnerContainer);

        // 🟢 Step 3: Dashboard
        dashboard = new PlayersDashboard();

        for (Player p : players) {
            dashboard.addPlayer(p.getName());
        }

        // 🟢 Step 4: Tokens
        createTokens();

        // 🟢 Step 5: Engine
        engine = new GameEngine(board, players, tokens);

        turnManager = new TurnManager(
                engine,
                this::triggerNextTurn,
                this::checkGameEnd
        );

        movementController = new MovementController(
                board,
                engine,
                new MovementController.MovementCallback() {

                    @Override
                    public void processStep(Player p, BoardSpace s, boolean landing) {
                        GameController.this.processStep(p, s, landing);
                    }

                    @Override
                    public void flushPending(Player p) {
                        flushPendingEvents(p);
                    }

                    @Override
                    public void finishTurn() {
                        if (!stopHandler.isInProgress()) {
                            GameController.this.finishTurn();
                        }
                    }

                    @Override
                    public void handleRetirement(Player p) {
                        GameController.this.handleRetirement(p);
                    }
                }
        );

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

                PlayerToken token =
                        tokens.get(players.indexOf(player));

                movementController.move(player, token, steps);
            }

            @Override
            public void requestSpinForStop() {
                handleSpinClick();
            }

            @Override
            public void endTurn() {
                finishTurn();
//                spinButton.setDisable(false);
            }

            @Override
            public void endGame(Player winner) {
                instantWinner = winner;
                tycoonWin = true;
                showWinner(winner);
                spinButton.setDisable(true);
            }
        });

        spaceResolver = new SpaceResolver(
                whiteHandler,
                redHandler,
                jumpHandler,
                stopHandler
        );

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

        Player player = engine.getCurrentPlayer();
        PlayerToken token = engine.getCurrentToken();

        spinnerController.spin(steps -> {

            System.out.println(player.getName() + " Spun: " + steps);

            if (stopHandler.isInProgress()) {
                stopHandler.handleSpin(steps);
            } else {
                movementController.move(player, token, steps);
            }
        });
    }

    private void finishTurn() {
        turnManager.finishTurn();
        spinButton.setDisable(false);
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
        finishTurn();
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

    private void processStep(
            Player player,
            BoardSpace space,
            boolean isLanding
    ) {
        spaceResolver.resolve(player, space, isLanding);
        dashboard.refresh(players);
    }

    private void flushPendingEvents(Player player) {
        spaceResolver.flush(player);
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
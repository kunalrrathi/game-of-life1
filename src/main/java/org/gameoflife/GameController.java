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

            int steps = new Random().nextInt(10) + 1;
            System.out.println("Spun: " + steps);

            Player currentPlayer = engine.getCurrentPlayer();
            PlayerToken token = engine.getCurrentToken();

            spinButton.setDisable(true);

            board.animateMovement(
                    token,
                    steps,
                    (space, remainingSteps) -> {

                        if ("Split".equalsIgnoreCase(space.getSpaceType())) {

                            board.stopAnimation();

                            Platform.runLater(() ->
                                    handleSplit(space, remainingSteps)
                            );
                        }
                    },
                    () -> {
                        BoardSpace landed = board.getSpace(token.getCurrentIndex());
                        handleLanding(currentPlayer, landed);
                        engine.nextTurn();
                        spinButton.setDisable(false);
                    }
            );
        });
    }

    // =========================================================
    // 🎯 LANDING LOGIC (CSV DRIVEN)
    // =========================================================

    private void handleLanding(Player player, BoardSpace space) {



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
                // You can later replace with salary logic
                player.collect(10000);
                break;

            case "Spin-Again":
                // Allow same player again
                return;

            case "Wait-Turn":
                // Skip next turn (we’ll implement later)
                break;

            case "Business":
                System.out.println("Business path selected (future logic)");
                break;

            case "University":
                System.out.println("University path selected (future logic)");
                break;
        }

        // 🔄 Refresh UI
        dashboard.refresh(players);
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

            token.setCurrentIndex(nextIndex);

            BoardSpace next = board.getSpace(nextIndex);

            token.getNode().setLayoutX(next.getX());
            token.getNode().setLayoutY(next.getY());

            // 🔥 KEY FIX: resume movement
            if (remainingSteps > 0) {

                board.animateMovement(
                        token,
                        remainingSteps,

                        // 🔁 STEP CALLBACK
                        (nextSpace, nextRemainingSteps) -> {

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
                            handleLanding(engine.getCurrentPlayer(), landed);
                            engine.nextTurn();
                            spinButton.setDisable(false);
                        }
                );

            } else {
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

    // =========================================================
    // 🎯 ROOT
    // =========================================================

    public BorderPane getRoot() {
        return root;
    }
}
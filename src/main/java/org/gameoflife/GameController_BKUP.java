//package org.gameoflife;
//
//import javafx.animation.KeyFrame;
//import javafx.animation.Timeline;
//import javafx.application.Platform;
//import javafx.geometry.Pos;
//import javafx.scene.control.*;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.Pane;
//import javafx.scene.paint.Color;
//import javafx.util.Duration;
//
//public class GameController_BKUP {
//
//    private BorderPane root;
//    private Pane gamePane;
//    Board board;
//    Player player;
//    GameEvent currentEvent;
//    private GameEngine engine;
//
//    private Label cashLabel;
//    private Button rollButton;
//    private Button repayButton;
//
//    private Spinner spinner;
//
//    Space space;
//
//    private javafx.scene.shape.Circle playerToken;
//
//    public GameController_BKUP() {
//
//        root = new BorderPane();
//        board = new Board();
//        player = new Player();
//        currentEvent = GameEvent.NONE;
//
//        engine = new GameEngine(board, player);
//        spinner = new Spinner();
//
//        setupUI();
//        placePlayer();
//        chooseRoute();
//    }
//
//    // ==========================
//    // UI SETUP
//    // ==========================
//
//    private void setupUI() {
//
//        cashLabel = new Label("Cash: $" + engine.getPlayer().getCash());
//
//        HBox topBar = new HBox(20, cashLabel);
//        topBar.setAlignment(Pos.CENTER);
//        root.setTop(topBar);
//
//        rollButton = new Button("Spin");
//
//        rollButton.setOnAction(e -> {
//
//            if (engine.getPlayer().isRetired()) return;
//
//            int steps = spinner.spin();
//            System.out.println("Spun: " + steps);
//            rollButton.setDisable(true);
//
//            animateMovement(steps);
//        });
//
//        repayButton = new Button("Repay Loan");
//
//        repayButton.setOnAction(e -> {
//            engine.repayLoan();
//            updateCashDisplay();
//        });
//
//        HBox controls = new HBox(10, rollButton, repayButton);
//        controls.setAlignment(Pos.CENTER);
//        root.setBottom(controls);
//
//        gamePane = new Pane();
//        gamePane.getChildren().add(engine.getBoard().getBoardPane());
//
//        playerToken = new javafx.scene.shape.Circle(10, Color.DARKBLUE);
//        gamePane.getChildren().add(playerToken);
//
//        root.setCenter(gamePane);
//    }
//
//    // ==========================
//    // ANIMATION
//    // ==========================
//
//    private void animateMovement(int steps) {
//
//        Timeline timeline = new Timeline();
//
//        // Disable spin while animating
//        rollButton.setDisable(true);
//
//        for (int i = 0; i < steps; i++) {
//
//            KeyFrame frame = new KeyFrame(
//                    Duration.millis(150 * (i + 1)),
//                    event -> {
//
//                        Space space = engine.moveOneStep();
//                        placePlayer();
//
//                        Space currentSpace = board.getSpace(player.getPosition());
//                        System.out.println("Passed: " + currentSpace.getType());
//
//                        // PASS logic
//                        engine.handlePassEvent(currentSpace);
//
//                        // STOP logic
//                        if (currentSpace.isStop()) {
//                            timeline.stop();
//                            currentEvent = engine.handleLandEvent(currentSpace);
//                            handleGameEvent(currentEvent);
//                            rollButton.setDisable(false);
//                        }
//
//                        if (player.isRetired()) {
//                            timeline.stop();
//                        }
//                    }
//            );
//
//            timeline.getKeyFrames().add(frame);
//        }
//
//        timeline.setOnFinished(e -> {
//
//            // Get final position AFTER animation
//            Space finalSpace = engine.getCurrentSpace();
//
//            GameEvent event = engine.handleLandEvent(finalSpace);
//
//            updateCashDisplay();
//
//            // Delay dialog safely
//            Platform.runLater(() -> {
//
//                handleGameEvent(event);
//
//                // Re-enable spin unless retired
//                if (!engine.getPlayer().isRetired()) {
//                    rollButton.setDisable(false);
//                }
//            });
//        });
//
//        timeline.play();
//    }
//
//    // ==========================
//    // GAME EVENT REACTION
//    // ==========================
//
//    private void handleGameEvent(GameEvent event) {
//
//        Platform.runLater(() -> {
//
//            switch (event) {
//
//                case MARRIAGE_STOP -> {
//                    showMarriageDialog();
//                    rollButton.setDisable(false);
//                }
//
//                case UNIVERSITY_ASSIGNMENT -> {
//                    assignUniversityProfessionUI();
//                    rollButton.setDisable(false);
//                }
//
//                case RETIREMENT -> {
//                    handleRetirementChoice();
//                }
//
//                default -> {
//                    rollButton.setDisable(false);
//                }
//            }
//
//        });
//    }
//
//    private void showMarriageDialog() {
//
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Marriage");
//        alert.setHeaderText("STOP: Marriage");
//        alert.setContentText("You got married!");
//
//        alert.showAndWait();
//    }
//
//    // ==========================
//    // UNIVERSITY SPIN LOGIC
//    // ==========================
//
//    private void handleUniversitySpin() {
//
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("University");
//        alert.setHeaderText("Spin to determine your profession!");
//        alert.setContentText("Click OK to spin.");
//
//        alert.showAndWait();
//
//        int steps = spinner.spin();
//
//        engine.universitySpin(steps);
//
//        placePlayer();
//        updateCashDisplay();
//
//        rollButton.setDisable(false);
//    }
//
//    private void assignUniversityProfessionUI() {
//
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("University Assignment");
//        alert.setHeaderText("Spin to determine your profession!");
//
//        ButtonType spinButton = new ButtonType("Spin");
//        alert.getButtonTypes().setAll(spinButton);
//
//        alert.showAndWait();
//
//        // Spin using engine
//        int result = spinner.spin();
//        System.out.println("University Spin: " + result);
//
//        engine.universitySpin(result);
//
//        updateCashDisplay();
//    }
//
//    // ==========================
//    // RETIREMENT UI
//    // ==========================
//
//    private void handleRetirementChoice() {
//
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Day of Reckoning");
//        alert.setHeaderText("Choose your retirement location");
//
//        ButtonType millionaire = new ButtonType("Millionaire Estates");
//        ButtonType countryside = new ButtonType("Countryside Acres");
//
//        alert.getButtonTypes().setAll(millionaire, countryside);
//
//        alert.showAndWait().ifPresent(choice -> {
//
//            if (choice == millionaire) {
//                engine.applyMillionaireEstatesBonus();
//            } else {
//                engine.applyCountrysideBonus();
//            }
//
//            updateCashDisplay();
//            showFinalResults();
//        });
//
//        rollButton.setDisable(true);
//        repayButton.setDisable(true);
//    }
//
//    private void showFinalResults() {
//
//        int netWorth = engine.getPlayer().getCash();
//
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Final Results");
//        alert.setHeaderText("Game Over");
//        alert.setContentText("Final Net Worth: $" + netWorth);
//
//        alert.showAndWait();
//    }
//
//    // ==========================
//    // ROUTE CHOICE
//    // ==========================
//
//    private void chooseRoute() {
//
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Choose Route");
//        alert.setHeaderText("Select Your Career Path");
//
//        ButtonType business = new ButtonType("Business");
//        ButtonType university = new ButtonType("University");
//
//        alert.getButtonTypes().setAll(business, university);
//
//        alert.showAndWait().ifPresent(response -> {
//
//            if (response == business) {
//                engine.assignProfession(Profession.BUSINESS);
//            } else {
//                engine.getPlayer().setUniversityRoute(true);
//            }
//        });
//    }
//
//    // ==========================
//    // UI HELPERS
//    // ==========================
//
//    private void placePlayer() {
//
//        Space space = engine.getCurrentSpace();
//
//        playerToken.setCenterX(space.getX());
//        playerToken.setCenterY(space.getY());
//    }
//
//    private void updateCashDisplay() {
//        cashLabel.setText("Cash: $" + engine.getPlayer().getCash());
//    }
//
//    public BorderPane getRoot() {
//        return root;
//    }
//}
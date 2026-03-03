package org.gameoflife;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.layout.HBox;

import java.util.List;

public class GameController {

    private BorderPane root;
    private Board board;
    private Player player;
    private Spinner spinner;
    private Pane gamePane;

    private int totalTiles;
    private Label cashLabel;
    private static final int UNIVERSITY_ASSIGNMENT_TILE = 10;

    private Button rollButton;
    private Button repayButton;

    public GameController() {
        root = new BorderPane();
        board = new Board();
        player = new Player(Color.RED);
        spinner = new Spinner();
        totalTiles = board.getPathSize();

//        player.borrowMoney(20000);

        setupUI();
        placePlayer();
        chooseRoute();
    }

    private void setupUI() {

        // --- Top Bar (Create ONCE) ---
        cashLabel = new Label("Cash: $" + player.getCash());

        HBox topBar = new HBox(20, cashLabel);
        topBar.setAlignment(Pos.CENTER);

        root.setTop(topBar);

        // --- Spin Button ---
        rollButton = new Button("Spin");

        rollButton.setOnAction(e -> {

            if (player.isRetired()) {
                return;   // 🔥 HARD STOP
            }

            int steps = spinner.spin();
            System.out.println("Spun: " + steps);

            rollButton.setDisable(true);
            animateMovement(steps, rollButton);
        });

        // --- Repay Button ---
        repayButton = new Button("Repay Loan");

        repayButton.setOnAction(e -> {
            player.repayLoan();
            updateCashDisplay();
        });

        // --- Bottom Controls ---
        HBox controls = new HBox(10, rollButton, repayButton);
        controls.setAlignment(Pos.CENTER);

        root.setBottom(controls);

        // --- Game Board Center ---
        gamePane = new Pane();
        gamePane.getChildren().add(board.getBoardPane());
        gamePane.getChildren().add(player.getToken());

        root.setCenter(gamePane);
    }

    private void animateMovement(int steps, Button rollButton) {
        Timeline timeline = new Timeline();
        for (int i = 0; i < steps; i++) {
            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(150 * (i + 1)),
                    event -> {
                        player.move(1, totalTiles);
                        placePlayer();

                        Space currentSpace = board.getSpace(player.getPosition());

                        // PASS logic
                        handlePassEvent(currentSpace);

                        // STOP logic
                        if (currentSpace.isStop()) {
                            timeline.stop();
                            handleLandEvent(currentSpace);
                            rollButton.setDisable(false);
                        }

                        if (player.isRetired()) {
                            timeline.stop();
                        }
                    }
            );

            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.setOnFinished(e -> {

            if (player.isRetired()) {
                rollButton.setDisable(true);

                Platform.runLater(() -> showGameOverDialog());
            } else {
                rollButton.setDisable(false);
            }
        });

        timeline.play();
    }


    private void placePlayer() {

        Space space = board.getSpace(player.getPosition());

        player.getToken().setCenterX(space.getX());
        player.getToken().setCenterY(space.getY());
    }

    private void chooseRoute() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Choose Route");
        alert.setHeaderText("Select Your Career Path");
        alert.setContentText("Choose your route:");

        ButtonType business = new ButtonType("Business");
        ButtonType university = new ButtonType("University");

        alert.getButtonTypes().setAll(business, university);

        alert.showAndWait().ifPresent(response -> {
            if (response == business) {
                player.setProfession(Profession.BUSINESS);
                player.setUniversityRoute(false);
                System.out.println("Chose BUSINESS route. Salary: 12000");
            } else {
                player.setUniversityRoute(true);
                System.out.println("Chose UNIVERSITY route.");
            }
        });
    }

    private void updateCashDisplay() {
        cashLabel.setText("Cash: $" + player.getCash());
    }

    private void handlePassEvent(Space space) {

        if (space.getType() == SpaceType.PAYDAY) {
            System.out.println("PASS PAYDAY → collect salary");
            if (!player.isRetired()) {
                player.addCash(player.getSalary());
            }
            player.payInterest();
            updateCashDisplay();
        }
        if (player.isUniversityRoute()
                && player.getProfession() == Profession.NONE
                && player.getPosition() == UNIVERSITY_ASSIGNMENT_TILE) {

            assignUniversityProfession();
        }
    }

    private void assignUniversityProfession() {

        List<Profession> options = List.of(
                Profession.DOCTOR,
                Profession.LAWYER,
                Profession.JOURNALIST,
                Profession.TEACHER,
                Profession.PHYSICIST
        );

        ChoiceDialog<Profession> dialog =
                new ChoiceDialog<>(options.get(0), options);

        dialog.setTitle("Choose Profession");
        dialog.setHeaderText("Select Your Profession");
        dialog.setContentText("Profession:");

        Platform.runLater(() -> {
            dialog.showAndWait().ifPresent(selected -> {
                player.setProfession(selected);
                System.out.println("Assigned Profession: "
                        + selected
                        + " Salary: "
                        + selected.getSalary());
            });
        });
    }

    private void showGameOverDialog() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("You have retired!");
        alert.setContentText("Final Cash: $" + player.getCash());

        alert.showAndWait();
    }

    private void handleRetirementChoice() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Day of Reckoning");
        alert.setHeaderText("Choose your retirement location");
        alert.setContentText("Where do you want to retire?");

        ButtonType millionaire = new ButtonType("Millionaire Estates");
        ButtonType countryside = new ButtonType("Countryside Acres");

        alert.getButtonTypes().setAll(millionaire, countryside);

        alert.showAndWait().ifPresent(choice -> {

            if (choice == millionaire) {
                handleMillionaireEstates();
            } else {
                handleCountryside();
            }

            showFinalResults();
        });
        rollButton.setDisable(true);
        repayButton.setDisable(true);
    }

    private void handleMillionaireEstates() {

        int bonus = (int)(Math.random() * 100_000);
        player.addCash(bonus);

        System.out.println("Millionaire Estates bonus: " + bonus);
    }

    private void handleCountryside() {

        int bonus = 50_000;
        player.addCash(bonus);

        System.out.println("Countryside bonus: " + bonus);
    }

    private void showFinalResults() {

        int netWorth = player.getCash();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Final Results");
        alert.setHeaderText("Game Over");
        alert.setContentText("Final Net Worth: $" + netWorth);

        alert.showAndWait();
        rollButton.setDisable(true);
        repayButton.setDisable(true);
    }

    private void handleLandEvent(Space space) {

        switch (space.getType()) {

            case TAX -> {
                System.out.println("LAND TAX → pay tax");
            }

            case MARRIAGE -> {
                System.out.println("STOP: Marriage");
            }

            case RETIRE -> {

                if (player.isRetired()) return;

                System.out.println("RETIREMENT reached!");

                player.setRetired(true);
                player.settleLoansAtRetirement();

                rollButton.setDisable(true);

                Platform.runLater(() -> handleRetirementChoice());

                updateCashDisplay();
            }

            case PAYDAY -> {
                System.out.println("LAND PAYDAY → collect salary");
                if (!player.isRetired()) {
                    player.addCash(player.getSalary());
                }
                player.payInterest();
                updateCashDisplay();
            }

            default -> {
                System.out.println("Normal space");
            }
        }
    }

    public BorderPane getRoot() {
        return root;
    }
}

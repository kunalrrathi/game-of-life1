package org.gameoflife;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class MovementController {

    public interface MovementCallback {
        void processStep(Player player, BoardSpace space, boolean isLanding);
        void flushPending(Player player);
        void finishTurn();
        void handleRetirement(Player player);
    }

    private final Board board;
    private final GameEngine engine;
    private final MovementCallback callback;

    public MovementController(
            Board board,
            GameEngine engine,
            MovementCallback callback
    ) {
        this.board = board;
        this.engine = engine;
        this.callback = callback;
    }

    public void move(Player player, PlayerToken token, int steps) {

        if (token.getCurrentIndex() < 0) {
            callback.finishTurn();
            return;
        }

        board.animateMovement(
                token,
                steps,

                (space, remainingSteps) ->
                        handleStep(player, token, space, remainingSteps),

                () -> handleLanding(player, token)
        );
    }

    private void handleStep(
            Player player,
            PlayerToken token,
            BoardSpace space,
            int remainingSteps
    ) {

        boolean isFinalSpace =
                remainingSteps == 0;

        // =====================================================
        // STOP SPACE
        // Stop immediately when reached
        // =====================================================

        if ("Stop".equalsIgnoreCase(space.getColor())) {

            board.stopAnimation();

            callback.processStep(
                    player,
                    space,
                    true
            );

            return;
        }

        // =====================================================
        // SPLIT SPACES
        // Only trigger on landing
        // =====================================================

        if (!isFinalSpace) {

            // =====================================================
            // SPLIT SPACES
            // Trigger immediately when reached
            // =====================================================

            switch (space.getSpaceType()) {

                case "Split":

                    board.stopAnimation();

                    Platform.runLater(() ->
                            handleCareerSplit(
                                    player,
                                    token,
                                    space,
                                    remainingSteps
                            ));

                    return;

                case "Split1":

                    board.stopAnimation();

                    Platform.runLater(() ->
                            handleGenericSplit(
                                    player,
                                    token,
                                    space,
                                    remainingSteps
                            ));

                    return;
            }
        }

        // =====================================================
        // PASS EVENTS
        // Skip final landing space
        // =====================================================

        if (!isFinalSpace) {

            // -------------------------------------------------
            // INTERACTIVE WHITE SPACE
            // Pause → Decision → Resume
            // -------------------------------------------------

            if (isInteractiveWhite(space)) {

                board.stopAnimation();

                Platform.runLater(() -> {

                    callback.processStep(
                            player,
                            space,
                            true
                    );

                    move(
                            player,
                            token,
                            remainingSteps
                    );
                });

                return;
            }

            // -------------------------------------------------
            // NORMAL PASS PROCESSING
            // -------------------------------------------------

            callback.processStep(
                    player,
                    space,
                    false
            );
        }
    }

    private void handleLanding(Player player, PlayerToken token) {

        if (token.getCurrentIndex() == -1) {

            callback.handleRetirement(player);
            callback.finishTurn();
            return;
        }

        BoardSpace landed =
                board.getSpace(token.getCurrentIndex());

        callback.processStep(player, landed, true);

//        callback.flushPending(player);

        callback.finishTurn();
    }

    private void handleCareerSplit(
            Player player,
            PlayerToken token,
            BoardSpace space,
            int remainingSteps
    ) {

        int nextIndex;

        if (player.isComputer()) {

            boolean university =
                    engine.getDecisionEngine()
                            .chooseUniversityPath(player);

            nextIndex = university
                    ? findNextByType("Main", space.getIndex())
                    : findNextByType("Shortcut", space.getIndex());

        } else {

            Alert alert =
                    new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle("Choose Path");
            alert.setHeaderText("Select your Career Path");

            ButtonType business =
                    new ButtonType("Business Route");

            ButtonType university =
                    new ButtonType("University Route");

            alert.getButtonTypes()
                    .setAll(business, university);

            ButtonType result =
                    alert.showAndWait().orElse(business);

            nextIndex =
                    result == university
                            ? findNextByType("Main", space.getIndex())
                            : findNextByType("Shortcut", space.getIndex());
        }

        continueAfterSplit(
                player,
                token,
                nextIndex,
                remainingSteps
        );
    }

    private void handleGenericSplit(
            Player player,
            PlayerToken token,
            BoardSpace splitSpace,
            int remainingSteps
    ) {

        // --------------------------------------------------
        // Find both branch starting points
        // --------------------------------------------------

        int path1Start =
                splitSpace.getNextIndex();

        int path2Start =
                splitSpace.getBranch();

        // --------------------------------------------------
        // Calculate preview destinations
        // (ONLY for popup display)
        // --------------------------------------------------

        int preview1 =
                moveForward(path1Start, remainingSteps);

        int preview2 =
                moveForward(path2Start, remainingSteps);

        BoardSpace space1 =
                board.getSpace(preview1);

        BoardSpace space2 =
                board.getSpace(preview2);

        String option1 =
                describeSpace(space1);

        String option2 =
                describeSpace(space2);

        // --------------------------------------------------
        // AI CHOICE
        // --------------------------------------------------

        if (player.isComputer()) {

            int chosenPathStart =
                    Math.random() < 0.5
                            ? path1Start
                            : path2Start;

            token.setCurrentIndex(chosenPathStart);

            move(
                    player,
                    token,
                    remainingSteps
            );

            return;
        }

        // --------------------------------------------------
        // HUMAN CHOICE
        // --------------------------------------------------

        Alert alert =
                new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Choose Your Path");

        alert.setHeaderText(
                "Select your route"
        );

        ButtonType path1Btn =
                new ButtonType(option1);

        ButtonType path2Btn =
                new ButtonType(option2);

        alert.getButtonTypes().setAll(
                path1Btn,
                path2Btn
        );

        ButtonType result =
                alert.showAndWait()
                        .orElse(path1Btn);

        int chosenPathStart =
                result == path1Btn
                        ? path1Start
                        : path2Start;

        token.setCurrentIndex(chosenPathStart);

        move(
                player,
                token,
                remainingSteps
        );
    }

    private int moveForward(
            int startIndex,
            int steps
    ) {

        int current = startIndex;

        for (int i = 0; i < steps; i++) {

            BoardSpace currentSpace =
                    board.getSpace(current);

            current =
                    currentSpace.getNextIndex();
        }

        return current;
    }

    private String describeSpace(BoardSpace s) {

        String action = s.getAction();

        if (action == null) {
            return "Continue";
        }

        switch (action) {

            case "Collect":
                return "💰 Collect ₹" + s.getAmount();

            case "Pay":
                return "💸 Pay ₹" + s.getAmount();

            case "Child":
                return "👶 Child is born";

            case "Twins":
                return "👶👶 Twins";

            case "Revenge":
                return "😈 Revenge";

            case "Lucky-Day":
                return "🍀 Lucky Day";

            default:
                if(s.getAmount() > 0)
                    return action + " (₹" + s.getAmount() + ")";
                else
                    return action;
        }
    }

    private void continueAfterSplit(
            Player player,
            PlayerToken token,
            int nextIndex,
            int remainingSteps
    ) {

        token.setCurrentIndex(nextIndex);

        BoardSpace next = board.getSpace(nextIndex);

        token.getNode().setLayoutX(next.getX());
        token.getNode().setLayoutY(next.getY());

        if (remainingSteps > 0) {
            move(player, token, remainingSteps);
        } else {
            handleLanding(player, token);
        }
    }

    private int findNextByType(
            String type,
            int fromIndex
    ) {

        for (int i = fromIndex + 1; i < 200; i++) {

            BoardSpace s = board.getSpace(i);

            if (type.equalsIgnoreCase(s.getSpaceType())) {
                return s.getIndex();
            }
        }

        return fromIndex + 1;
    }

    private boolean isInteractiveWhite(BoardSpace space) {

        if (!"White".equalsIgnoreCase(space.getColor())) {
            return false;
        }

        String action = space.getAction();

        return switch (action) {

            case "Pay-Life-Insurance",
                 "Pay-Auto-Insurance",
                 "Pay-Fire-Insurance",
                 "Pay-Stock-Insurance",
                 "Play-Market" -> true;

            default -> false;
        };
    }

    public PlayerToken getTokenForPlayer(Player player) {
        return engine.getTokenForPlayer(player);
    }

    public Board getBoard() {
        return board;
    }
}
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

        // STOP
        if ("Stop".equalsIgnoreCase(space.getColor())) {

            board.stopAnimation();

            callback.flushPending(player);

            callback.processStep(player, space, true);

            return;
        }

        // PASSING
        callback.processStep(player, space, false);

        // SPLIT
        if ("Split".equalsIgnoreCase(space.getSpaceType())) {

            board.stopAnimation();

            Platform.runLater(() ->
                    handleSplit(player, token, space, remainingSteps));
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

        callback.flushPending(player);

        callback.finishTurn();
    }

    private void handleSplit(
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
}
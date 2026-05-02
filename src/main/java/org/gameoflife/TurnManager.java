package org.gameoflife;

import javafx.application.Platform;

import java.util.function.Supplier;

public class TurnManager {

    private final GameEngine engine;
    private final Runnable triggerNextTurn;
    private final Supplier<Boolean> checkGameEnd;
    int safety = 0;

    private boolean isDecisionPending = false;

    public TurnManager(
            GameEngine engine,
            Runnable triggerNextTurn,
            Supplier<Boolean> checkGameEnd
    ) {
        this.engine = engine;
        this.triggerNextTurn = triggerNextTurn;
        this.checkGameEnd = checkGameEnd;
    }

    public void finishTurn() {

        // 🔒 BLOCK turn progression while a decision dialog is open
        if (isDecisionPending) return;

        engine.nextTurn();

        Player current = engine.getCurrentPlayer();

        int safety = 0; // make sure this exists

        // 🔥 Handle skipped turns
        while (current.shouldSkipTurn() && safety < 10) {

            System.out.println(current.getName() + " skips this turn.");

            current.reduceSkipTurn();

            engine.nextTurn();
            current = engine.getCurrentPlayer();

            safety++;
        }

        Platform.runLater(() -> {
            if (!checkGameEnd.get()) {
                triggerNextTurn.run();
            }
        });
    }

    public void setDecisionPending(boolean value) {
        this.isDecisionPending = value;
    }
}
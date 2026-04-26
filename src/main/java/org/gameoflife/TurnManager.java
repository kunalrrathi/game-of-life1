package org.gameoflife;

import javafx.application.Platform;

import java.util.function.Supplier;

public class TurnManager {

    private final GameEngine engine;
    private final Runnable triggerNextTurn;
    private final Supplier<Boolean> checkGameEnd;
    int safety = 0;

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

        engine.nextTurn();

        Player current = engine.getCurrentPlayer();

        // 🔥 Handle skipped turns
        while (current.shouldSkipTurn() && safety < 10) {

            System.out.println(current.getName() + " skips this turn.");

            current.reduceSkipTurn();

            engine.nextTurn();
            current = engine.getCurrentPlayer();
        }

        Platform.runLater(() -> {
            if (!checkGameEnd.get()) {
                triggerNextTurn.run();
            }
        });
    }
}
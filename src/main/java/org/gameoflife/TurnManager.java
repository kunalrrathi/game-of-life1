package org.gameoflife;

import javafx.application.Platform;

import java.util.function.Supplier;

public class TurnManager {

    private final GameEngine engine;
    private final Runnable triggerNextTurn;
    private final Supplier<Boolean> checkGameEnd;

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

        Platform.runLater(() -> {
            if (!checkGameEnd.get()) {
                triggerNextTurn.run();
            }
        });
    }
}
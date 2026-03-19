package org.gameoflife;

import java.util.List;

public class GameEngine {

    private Board board;
    private List<Player> players;
    private List<PlayerToken> tokens;

    private int currentPlayerIndex = 0;

    public GameEngine(Board board, List<Player> players, List<PlayerToken> tokens) {
        this.board = board;
        this.players = players;
        this.tokens = tokens;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public PlayerToken getCurrentToken() {
        return tokens.get(currentPlayerIndex);
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
}


package org.gameoflife;

import java.util.List;

public class GameEngine {

    private ComputerDecisionEngine decisionEngine = new ComputerDecisionEngine();
    private Board board;
    private List<Player> players;
    private List<PlayerToken> tokens;

    private Player firstMillionaire = null;
    private Integer luckyNumber = null;

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

        if (players.isEmpty()) return;

        int checked = 0;

        do {
            currentPlayerIndex =
                    (currentPlayerIndex + 1) % players.size();

            Player p = players.get(currentPlayerIndex);

            if (!p.isRetired() && !p.isBankrupt()) {
                return;
            }

            checked++;

        } while (checked <= players.size());
    }

    public boolean hasMillionaire() {
        return firstMillionaire != null;
    }

    public Player getFirstMillionaire() {
        return firstMillionaire;
    }

    public void setFirstMillionaire(Player player) {
        this.firstMillionaire = player;
    }

    public Integer getLuckyNumber() {
        return luckyNumber;
    }

    public void setLuckyNumber(int number) {
        this.luckyNumber = number;
    }

    public ComputerDecisionEngine getDecisionEngine() {
        return decisionEngine;
    }

    public PlayerToken getTokenForPlayer(Player player) {

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i) == player) {
                return tokens.get(i);
            }
        }

        return null; // safety fallback
    }
}


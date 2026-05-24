package org.gameoflife;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PlayerToken {

    private Circle token;
    private Player player;
    private int currentIndex = 0;
    Color color;

    private Integer forcedNextIndex;

    public PlayerToken(Player player, Color color) {

        this.player = player;

        token = new Circle(10);
        token.setFill(color);
        token.setStroke(Color.BLACK);

        this.color = color;
    }

    public Circle getNode() {
        return token;
    }

    public Player getPlayer() {
        return player;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

    public Color getColor() {
        return color;
    }

    public Integer getForcedNextIndex() {
        return forcedNextIndex;
    }

    public void setForcedNextIndex(Integer forcedNextIndex) {
        this.forcedNextIndex = forcedNextIndex;
    }
}
package org.gameoflife;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PlayerToken {

    private Circle token;
    private Player player;

    public PlayerToken(Player player, Color color) {

        this.player = player;

        token = new Circle(10);
        token.setFill(color);
        token.setStroke(Color.BLACK);
    }

    public Circle getNode() {
        return token;
    }

    public Player getPlayer() {
        return player;
    }
}
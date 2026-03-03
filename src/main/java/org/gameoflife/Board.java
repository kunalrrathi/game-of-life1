package org.gameoflife;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private Pane boardPane;
    private List<Space> path;

    private final int tileSpacing = 70;

    public Board() {
        boardPane = new Pane();
        path = new ArrayList<>();

        createPath();
        drawPath();
    }

    private void createPath() {

        int index = 0;
        double startX = 50;
        double startY = 50;

        int width = 10;
        int height = 10;

        for (int row = 0; row < height; row++) {

            for (int col = 0; col < width; col++) {

                double x;
                double y = startY + row * tileSpacing;

                if (row % 2 == 0) {
                    x = startX + col * tileSpacing;
                } else {
                    x = startX + (width - 1 - col) * tileSpacing;
                }

                SpaceType type;

                if (index == 0) {
                    type = SpaceType.START;
                } else if (index % 10 == 0) {
                    type = SpaceType.PAYDAY;
                } else if (index == 15) {
                    type = SpaceType.MARRIAGE;
                } else if (index == 95) {
                    type = SpaceType.RETIRE;
                } else {
                    type = SpaceType.NORMAL;
                }

                boolean isStop = (type == SpaceType.MARRIAGE) || (type == SpaceType.RETIRE);

                Integer branch = null;

                if (index == 0) {
                    branch = 8; // college path starts at index 8
                }

                path.add(new Space(index, type, isStop, x, y, branch));

                index++;
            }
        }
    }

    private void drawPath() {

        for (Space space : path) {

            Circle circle = new Circle(20);

            circle.setCenterX(space.getX());
            circle.setCenterY(space.getY());

            switch (space.getType()) {
                case START -> circle.setFill(Color.WHITE);
                case PAYDAY -> circle.setFill(Color.RED);
                case MARRIAGE -> circle.setFill(Color.ORANGE);
                case RETIRE -> circle.setFill(Color.YELLOW);
                default -> circle.setFill(Color.LIGHTYELLOW);
            }

            circle.setStroke(Color.BLACK);

            boardPane.getChildren().add(circle);
        }
    }

    public Space getSpace(int position) {
        return path.get(position);
    }

    public int getPathSize() {
        return path.size();
    }

    public Pane getBoardPane() {
        return boardPane;
    }
}

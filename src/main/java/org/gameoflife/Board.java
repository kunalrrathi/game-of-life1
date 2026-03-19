package org.gameoflife;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private List<BoardSpace> spaces;

    private Pane boardPane;

    private List<PlayerToken> tokens = new ArrayList<>();

    public Board() {

        boardPane = new Pane();

        // Load board spaces from CSV
        spaces = BoardLoader.loadBoard("src/main/resources/org/gameoflife/BoardGame-GOL.csv");

        drawBoard();
    }

    private void drawBoard() {

        // Background image
        ImageView boardImage = new ImageView(
                new Image(getClass().getResource("/org/gameoflife/life_board.jpg").toExternalForm())
        );

        boardImage.setFitWidth(800);
        boardImage.setPreserveRatio(true);

        boardPane.getChildren().add(boardImage);

        // Draw spaces (debug markers)
        for (BoardSpace space : spaces) {

            Circle marker = new Circle(15);

            marker.setLayoutX(space.getX());
            marker.setLayoutY(space.getY());

            marker.setFill(Color.YELLOW);
            marker.setStroke(Color.BLACK);

            boardPane.getChildren().add(marker);

            Text label = new Text(String.valueOf(space.getIndex()));
            label.setLayoutX(space.getX() - 7);
            label.setLayoutY(space.getY() + 5);
            boardPane.getChildren().add(label);
        }
    }

    public void positionTokens(List<PlayerToken> tokens, int spaceIndex) {

        BoardSpace space = spaces.get(spaceIndex);

        double baseX = space.getX();
        double baseY = space.getY();

        double[][] offsets = {
                {0,0},
                {-10,-10},
                {10,-10},
                {-10,10},
                {10,10},
                {0,15}
        };

        for(int i=0;i<tokens.size();i++){

            Circle node = tokens.get(i).getNode();

            node.setLayoutX(baseX + offsets[i][0]);
            node.setLayoutY(baseY + offsets[i][1]);
        }
    }

    public Pane getBoardPane() {
        return boardPane;
    }

    public BoardSpace getSpace(int index) {
        return spaces.get(index);
    }

    public int getTotalSpaces() {
        return spaces.size();
    }

    public List<BoardSpace> getSpaces() {
        return spaces;
    }

    public void addToken(PlayerToken token) {

        tokens.add(token);

        boardPane.getChildren().add(token.getNode());
    }

}
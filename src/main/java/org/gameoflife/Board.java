package org.gameoflife;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Board {

    private List<BoardSpace> spaces;

    private Pane boardPane;

    private List<PlayerToken> tokens = new ArrayList<>();
    private Timeline timeline;

//    private ImageView spinnerBase;
    private StackPane spinnerContainer;
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

//            spinnerContainer = new StackPane();
//
//            ImageView spinnerBase = new ImageView(new Image(getClass().getResource("/org/gameoflife/spinner.png").toExternalForm()));
//            spinnerBase.setFitWidth(270);
//            spinnerBase.setFitHeight(270);
//            spinnerBase.setLayoutX(230);
//            spinnerBase.setLayoutY(200);
//
//            spinnerContainer.setLayoutX(230);
//            spinnerContainer.setLayoutY(200);
//
//
//            spinnerContainer.setPrefSize(270, 270); // same as spinnerBase
//            spinnerContainer.getChildren().add(spinnerBase);
//
//            boardPane.getChildren().add(spinnerBase);
//
//            ImageView spinnerArrow = new ImageView(new Image(getClass().getResource("/org/gameoflife/arrow.png").toExternalForm()));
//            spinnerArrow.setFitWidth(80);
//            spinnerArrow.setFitHeight(80);
//            spinnerArrow.setLayoutX(220);
//            spinnerArrow.setLayoutY(310);
//
//            boardPane.getChildren().add(spinnerArrow);
        }

        // 🔥 CREATE SPINNER ONLY ONCE

        spinnerContainer = new StackPane();

        ImageView spinnerBase = new ImageView(
                new Image(getClass().getResource("/org/gameoflife/spinner.png").toExternalForm())
        );

        spinnerBase.setFitWidth(270);
        spinnerBase.setFitHeight(270);

// Container size
        spinnerContainer.setPrefSize(270, 270);

// Center spinner inside container
        spinnerContainer.getChildren().add(spinnerBase);

// Position container
        spinnerContainer.setLayoutX(240);
        spinnerContainer.setLayoutY(200);

// Add to board
        boardPane.getChildren().add(spinnerContainer);

// Arrow (static)
        ImageView spinnerArrow = new ImageView(
                new Image(getClass().getResource("/org/gameoflife/arrow.png").toExternalForm())
        );

        spinnerArrow.setFitWidth(80);
        spinnerArrow.setFitHeight(80);

// Position arrow
        spinnerArrow.setLayoutX(220);
        spinnerArrow.setLayoutY(310);

        boardPane.getChildren().add(spinnerArrow);
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

        System.out.println("Total spaces: " + spaces.size());
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

    public void stopAnimation() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    public void animateMovement(PlayerToken token, int steps, BiConsumer<BoardSpace, Integer> onStep, Runnable onFinished) {

        timeline = new Timeline();

        for (int i = 1; i <= steps; i++) {

            int step = i;

            KeyFrame frame = new KeyFrame(
                    Duration.millis(300 * step),
                    e -> {

                        BoardSpace current = getSpace(token.getCurrentIndex());

                        int nextIndex = current.getNextIndex();

                        // 🎯 HANDLE END OF BOARD (Retire)
                        if (nextIndex < 0) {

                            stopAnimation();

                            // Notify controller with current space
                            if (onStep != null) {
                                onStep.accept(current, 0);
                            }

                            if (onFinished != null) {
                                onFinished.run();
                            }

                            return;
                        }

                        // Safety guard
                        if (nextIndex >= spaces.size()) {
                            nextIndex = spaces.size() - 1;
                        }

                        token.setCurrentIndex(nextIndex);

                        BoardSpace space = spaces.get(nextIndex);

                        token.getNode().setLayoutX(space.getX());
                        token.getNode().setLayoutY(space.getY());

                        int remainingSteps = steps - (step + 1);

                        // 🔥 Notify controller at each step
                        if (onStep != null) {
                            onStep.accept(space, remainingSteps);
                        }
                    }
            );

            timeline.getKeyFrames().add(frame);
        }

        timeline.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });

        timeline.play();
    }

    public StackPane getSpinnerContainer() {
        return spinnerContainer;
    }
}
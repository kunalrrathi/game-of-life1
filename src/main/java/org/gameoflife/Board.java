package org.gameoflife;

import javafx.animation.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.HashMap;
import java.util.Map;

public class Board {

    private List<BoardSpace> spaces;

    private Pane boardPane;

    private List<PlayerToken> tokens = new ArrayList<>();
    private Pane highlightLayer;

    private Map<Integer, Circle> spaceHighlights = new HashMap<>();
    private final Map<PlayerToken, ScaleTransition> activeTokenAnimations
            = new HashMap<>();
    private Timeline timeline;

//    private ImageView spinnerBase;
    private StackPane spinnerContainer;
    public Board() {

        boardPane = new Pane();

        // 🔥 INIT HIGHLIGHT LAYER FIRST
        highlightLayer = new Pane();

        // Load board spaces
        spaces = BoardLoader.loadBoard(
                "src/main/resources/org/gameoflife/BoardGame-GOL.csv"
        );

        drawBoard();
    }

    private void drawBoard() {

        // Background image
        ImageView boardImage = new ImageView(
                new Image(getClass().getResource("/org/gameoflife/life_board.jpg").toExternalForm())
        );

        boardImage.setFitWidth(1400);
        boardImage.setFitHeight(980);
//        boardImage.setPreserveRatio(true);

        boardPane.getChildren().add(boardImage);

        // Add layer to board
        boardPane.getChildren().add(highlightLayer);

        // Draw spaces (debug markers)
        for (BoardSpace space : spaces) {

            Circle marker = new Circle(15);

            Circle glow = new Circle(20);

            glow.setLayoutX(space.getX());
            glow.setLayoutY(space.getY());

            glow.setFill(Color.TRANSPARENT);
            glow.setStrokeWidth(8);

            glow.setVisible(false);

            spaceHighlights.put(space.getIndex(), glow);

            highlightLayer.getChildren().add(glow);

            marker.setLayoutX(space.getX());
            marker.setLayoutY(space.getY());

            marker.setFill(Color.YELLOW);
            marker.setStroke(Color.BLACK);

//            boardPane.getChildren().add(marker); //Need to remove this later as it is just for debugging

            Text label = new Text(String.valueOf(space.getIndex()));
            label.setLayoutX(space.getX() - 7);
            label.setLayoutY(space.getY() + 5);
//            boardPane.getChildren().add(label); //Need to remove this later as it is just for debugging
        }

        // Add spinner base (static)
        ImageView spinnerBase = new ImageView(
                new Image(getClass().getResource("/org/gameoflife/spinner.png").toExternalForm())
        );

        spinnerBase.setFitWidth(370);
        spinnerBase.setFitHeight(370);

        spinnerBase.setLayoutX(280);
        spinnerBase.setLayoutY(340);

        // Add to board
        boardPane.getChildren().add(spinnerBase);

        // 🔥 CREATE SPINNER ONLY ONCE
        spinnerContainer = new StackPane();

        // Arrow (dynamic, rotates)
        ImageView spinnerArrow = new ImageView(
                new Image(getClass().getResource("/org/gameoflife/arrow.png").toExternalForm())
        );

        spinnerArrow.setFitWidth(100);
        spinnerArrow.setFitHeight(100);

        // Position container
        spinnerContainer.setLayoutX(415);
        spinnerContainer.setLayoutY(460);

        spinnerContainer.getChildren().add(spinnerArrow);

        boardPane.getChildren().add(spinnerContainer);
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

                            token.setCurrentIndex(-1); // 🔥 mark retired/end reached

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

                        Color trailColor =
                                (Color) token.getNode().getFill();

                        highlightTrail(
                                space.getIndex(),
                                trailColor
                        );

                        // 🔥 Notify controller at each step
                        if (onStep != null) {
                            onStep.accept(space, remainingSteps);
                        }
                    }
            );

            timeline.getKeyFrames().add(frame);
        }

        timeline.setOnFinished(e -> {

            if (token.getCurrentIndex() >= 0) {

                BoardSpace landed =
                        getSpace(token.getCurrentIndex());

                Color playerColor =
                        (Color) token.getNode().getFill();

                highlightSpace(
                        landed.getIndex(),
                        playerColor
                );
            }

            if (onFinished != null) {
                onFinished.run();
            }
        });

        timeline.play();
    }

    public StackPane getSpinnerContainer() {
        return spinnerContainer;
    }

    public void highlightSpace(
            int index,
            Color color
    ) {

        Circle glow = spaceHighlights.get(index);

        if (glow == null) return;

        glow.setStroke(color);
        glow.setOpacity(1.0);
        glow.setScaleX(1.0);
        glow.setScaleY(1.0);

        glow.setVisible(true);

        // 🔥 Pulse animation
        ScaleTransition pulse =
                new ScaleTransition(Duration.millis(400), glow);

        pulse.setFromX(1.0);
        pulse.setToX(1.5);

        pulse.setFromY(1.0);
        pulse.setToY(1.5);

        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);

        // 🔥 Fade animation
        FadeTransition fade =
                new FadeTransition(Duration.millis(1400), glow);

        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        fade.setOnFinished(e -> {
            glow.setVisible(false);
            glow.setOpacity(1.0);
        });

        pulse.play();
        fade.play();
    }

    public Color getSpaceHighlightColor(BoardSpace space) {

        if (space == null) return Color.LIMEGREEN;

        String action = space.getAction();

        if (action == null) {
            return Color.LIMEGREEN;
        }

        return switch (action) {

            case "Revenge" -> Color.MEDIUMPURPLE;

            case "Lucky-Day" -> Color.GOLD;

            case "Marriage" -> Color.HOTPINK;

            case "Retire" -> Color.GOLD;

            case "Pay" -> Color.RED;

            case "Collect" -> Color.LIMEGREEN;

            default -> Color.DODGERBLUE;
        };
    }

    public void pulseToken(PlayerToken token) {

        ScaleTransition pulse =
                new ScaleTransition(Duration.millis(500), token.getNode());

        pulse.setFromX(1.0);
        pulse.setToX(1.3);

        pulse.setFromY(1.0);
        pulse.setToY(1.3);

        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);

        pulse.play();
    }

    public void highlightTrail(int index, Color color) {

        Circle glow = spaceHighlights.get(index);

        if (glow == null) return;

        glow.setStroke(color);

        glow.setOpacity(0.8);
        glow.setVisible(true);

        FadeTransition fade =
                new FadeTransition(Duration.millis(900), glow);

        fade.setFromValue(0.8);
        fade.setToValue(0.0);

        fade.setOnFinished(e -> {
            glow.setVisible(false);
        });

        fade.play();
    }

    public void startActiveTokenPulse(PlayerToken token) {

        stopActiveTokenPulse(token);

        ScaleTransition pulse =
                new ScaleTransition(Duration.millis(800), token.getNode());

        pulse.setFromX(1.0);
        pulse.setToX(1.18);

        pulse.setFromY(1.0);
        pulse.setToY(1.18);

        pulse.setCycleCount(Animation.INDEFINITE);

        pulse.setAutoReverse(true);

        pulse.play();

        activeTokenAnimations.put(token, pulse);
    }

    public void stopActiveTokenPulse(PlayerToken token) {

        ScaleTransition animation =
                activeTokenAnimations.remove(token);

        if (animation != null) {
            animation.stop();
        }

        token.getNode().setScaleX(1.0);
        token.getNode().setScaleY(1.0);
    }

    public Pane getView() {
        return boardPane;
    }
}
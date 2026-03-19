package org.gameoflife;

import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class GameController {

    private BorderPane root;

    private Board board;
    private PlayerPanel playerPanel;

    private List<Player> players;
    private PlayersDashboard dashboard;

    private List<PlayerToken> tokens = new ArrayList<>();

    public GameController() {

        root = new BorderPane();

        players = GameSetupDialog.showDialog();

        board = new Board();

        dashboard = new PlayersDashboard();

        for (Player p : players) {
            dashboard.addPlayer(p.getName());
        }

        Color[] colors = {
                Color.BLUE,
                Color.RED,
                Color.GREEN,
                Color.YELLOW,
                Color.PURPLE,
                Color.ORANGE
        };

        for(int i=0;i<players.size();i++){

            PlayerToken token = new PlayerToken(players.get(i), colors[i]);

            tokens.add(token);

            board.addToken(token);
        }

        board.positionTokens(tokens, 0);

        root.setCenter(board.getBoardPane());
        root.setRight(dashboard.getPanel());
    }

    public BorderPane getRoot() {
        return root;
    }
}
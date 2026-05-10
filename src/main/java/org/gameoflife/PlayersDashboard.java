package org.gameoflife;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class PlayersDashboard {

    private final TilePane root;
    private final ScrollPane scrollPane;

    private List<PlayerCard> playerCards;

    private List<Player> players;

    public PlayersDashboard() {

        root = new TilePane();
        root.setHgap(15);
        root.setVgap(15);
        root.setPadding(new Insets(10));
        root.setPrefColumns(2);
        root.setTileAlignment(Pos.TOP_LEFT);

        scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        scrollPane.setPrefWidth(420);

        playerCards = new ArrayList<>();
        players = new ArrayList<>();
    }

    public void addPlayer(Player player, PlayerToken token) {

        PlayerCard card = new PlayerCard(player, token);

        playerCards.add(card);

        root.getChildren().add(card.getCard());
    }

    public void refresh(List<Player> players) {

        for (int i = 0; i < players.size(); i++) {

            Player player = players.get(i);
            PlayerCard card = playerCards.get(i);

            card.update(player);
        }
    }

    public Node getView() {
        return scrollPane;
    }

    public void setActivePlayer(Player activePlayer) {

        for (PlayerCard card : playerCards) {

            boolean active =
                    card.getPlayer() == activePlayer;

            card.setActive(active);
        }
    }
}
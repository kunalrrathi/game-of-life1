package org.gameoflife;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.List;

public class JumpSpaceHandler {

    private Board board;
    private GameEngine engine;
    private PlayersDashboard dashboard;

    public JumpSpaceHandler(Board board, GameEngine engine, PlayersDashboard dashboard) {
        this.board = board;
        this.engine = engine;
        this.dashboard = dashboard;
    }

    public void handle(Player player, BoardSpace space) {

        String action = space.getAction();
        if (action != null) action = action.trim();

        if (action == null) return;

        // 🎯 Special case
        if ("Go-To-Start".equalsIgnoreCase(action)) {
            moveToTarget(player, space);
            return;
        }

        // 🎯 Map action → Profession
        Profession profession = mapToProfession(action);

        if (profession != null) {

            player.setProfession(profession);
            player.addCash(profession.getSalary());

            System.out.println(player.getName() +
                    " chose career: " + profession +
                    " | Salary: " + profession.getSalary());

            // 🎉 Popup
            if (!player.isComputer()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Career Selected");
                    alert.setHeaderText(null);
                    alert.setContentText(player.getName() +
                            " is now a " + profession +
                            "\nSalary: " + profession.getSalary());
                    alert.showAndWait();
                });
            } else {
                System.out.println(player.getName() +
                        " (AI) is now a " + profession +
                        " | Salary: " + profession.getSalary());
            }
        }

        // 🎯 Move player
        moveToTarget(player, space);

        dashboard.refresh(List.of(player));
    }

    // =========================================================
    // 🎯 HELPERS
    // =========================================================

    private Profession mapToProfession(String action) {

        switch (action) {
            case "Doctor": return Profession.DOCTOR;
            case "Lawyer": return Profession.LAWYER;
            case "Journalist": return Profession.JOURNALIST;
            case "Teacher": return Profession.TEACHER;
            case "Physicist": return Profession.PHYSICIST;
            case "University": return Profession.UNIVERSITY_DEGREE;
            case "Business": return Profession.BUSINESS;
            default: return null;
        }
    }

    private void moveToTarget(Player player, BoardSpace space) {

        Integer targetIndex = space.getBranch();
        if (targetIndex == null) return;

        // Update model
        player.setPosition(targetIndex);

        // Update UI
        PlayerToken token = engine.getCurrentToken();
        token.setCurrentIndex(targetIndex);

        BoardSpace target = board.getSpace(targetIndex);

        token.getNode().setLayoutX(target.getX());
        token.getNode().setLayoutY(target.getY());
    }
}
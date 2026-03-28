package org.gameoflife;

import java.util.List;

public class RedSpaceHandler {

    private PlayersDashboard dashboard;

    public RedSpaceHandler(PlayersDashboard dashboard) {
        this.dashboard = dashboard;
    }

    public void handle(Player player, BoardSpace space) {

        String action = space.getAction();
        if (action != null) action = action.trim();

        if (action == null) return;

        switch (action) {

            case "PayDay":
                player.collect(player.getSalary());
                break;

            case "Collect":
                player.collect(space.getAmount());
                break;

            case "Pay":
                player.pay(space.getAmount());
                break;

            case "Wait-Turn":
                // future logic
                break;

            case "Business":
                handleBusiness(player);
                break;

            case "Accident":
                // handled via GameController (claim logic)
                break;

            case "Fire":
                break;

            case "Stock-Crash":
                break;
        }

        dashboard.refresh(List.of(player));
    }

    private void handleBusiness(Player player) {

        if (player.getProfession() == Profession.NONE) {

            player.setProfession(Profession.BUSINESS);
            player.addCash(Profession.BUSINESS.getSalary());

            System.out.println(player.getName() +
                    " profession set to BUSINESS with salary " + player.getSalary());

        } else {

            System.out.println(player.getName() +
                    " already has a profession → " + player.getProfession());
        }
    }
}
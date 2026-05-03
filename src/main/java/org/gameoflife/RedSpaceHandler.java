package org.gameoflife;

import java.util.List;
import static utilities.LogColors.*;

public class RedSpaceHandler {

    private PlayersDashboard dashboard;
    private GameLogPanel logPanel;

    public RedSpaceHandler(PlayersDashboard dashboard, GameLogPanel logPanel) {
        this.dashboard = dashboard;
        this.logPanel = logPanel;
    }

    public void handle(Player player, BoardSpace space) {

        String action = space.getAction();
        if (action != null) action = action.trim();

        if (action == null) return;

        switch (action) {

            case "PayDay":
                checkProfessionAndCollectSalary(player);
                break;

            case "Collect-Red":
                player.collect(space.getAmount());
                logPanel.log(player.getName() + ": Collect: " + space.getAmount() + " | Remaining Cash: " + player.getCash(), COLLECT);
                break;

            case "Pay-Red":
                player.pay(space.getAmount());
                logPanel.log(player.getName() + ": Paid: " + space.getAmount() + " | Remaining Cash: " + player.getCash(), PAY);
                break;

            case "Business":
                handleBusiness(player);
                break;

            case "Taxes":
                logPanel.log(player.getName() + " pays taxes of Half Salary: ", PAY);
                player.pay(player.getSalary()/2);
                logPanel.log(player.getName() + ": Paid: " + player.getSalary()/2 + " | Remaining Cash: " + player.getCash(), PAY);
                break;
        }

        dashboard.refresh(List.of(player));
    }

    private void checkProfessionAndCollectSalary(Player player) {
        if (player.getProfession() == Profession.NONE) {

            logPanel.log(player.getName() +
                    " has no profession, so assigning Profession as UNIVERSITY_DEGREE with salary " + Profession.UNIVERSITY_DEGREE.getSalary(), INFO);
            player.setProfession(Profession.UNIVERSITY_DEGREE);
            player.addCash(Profession.UNIVERSITY_DEGREE.getSalary());
        } else {
            logPanel.log("Yippee, PayDay !!: " + player.getName() +
                    " receives salary: " + player.getSalary(), COLLECT);
            player.collect(player.getSalary());
        }
    }

    private void handleBusiness(Player player) {

        if (player.getProfession() == Profession.NONE) {

            player.setProfession(Profession.BUSINESS);
            player.addCash(Profession.BUSINESS.getSalary());

            logPanel.log(player.getName() +
                    " profession set to BUSINESS with salary " + player.getSalary(), INFO);

        } else {

            System.out.println(player.getName() +
                    " already has a profession → " + player.getProfession());
        }
    }
}
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
                checkProfessionAndCollectSalary(player);
                break;
//                System.out.println("Yay, PayDay !!: " + player.getName() + " receives salary: " + player.getSalary());
//                player.collect(player.getSalary());
//                break;

            case "Collect-Red":
                player.collect(space.getAmount());
                break;

            case "Pay-Red":
                player.pay(space.getAmount());
                break;

            case "Business":
                handleBusiness(player);
                break;

            case "Taxes":
                System.out.println(player.getName() + " pays taxes of Half Salary: ");
                player.pay(player.getSalary()/2);
                break;

            case "Fire":
                break;

            case "Stock-Crash":
                break;
        }

        dashboard.refresh(List.of(player));
    }

    private void checkProfessionAndCollectSalary(Player player) {
        if (player.getProfession() == Profession.NONE) {

            System.out.println(player.getName() +
                    " has no profession, so assigning Profession as UNIVERSITY_DEGREE with salary " + Profession.UNIVERSITY_DEGREE.getSalary());
            player.setProfession(Profession.UNIVERSITY_DEGREE);
            player.addCash(Profession.UNIVERSITY_DEGREE.getSalary());
        } else {
            System.out.println("Yippee, PayDay !!: " + player.getName() +
                    " receives salary: " + player.getSalary());
            player.collect(player.getSalary());
        }
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
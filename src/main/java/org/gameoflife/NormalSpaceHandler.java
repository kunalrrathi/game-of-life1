package org.gameoflife;

import java.util.List;

public class NormalSpaceHandler {

    private final PlayersDashboard dashboard;
    private final SpinnerController spinnerController;

    public NormalSpaceHandler(
            PlayersDashboard dashboard,
            SpinnerController spinnerController
    ) {
        this.dashboard = dashboard;
        this.spinnerController = spinnerController;
    }

    public void handle(Player player, BoardSpace space) {

        String action = space.getAction();

        if (action == null) return;

        action = action.trim();

        switch (action) {

            case "Collect":
                player.collect(space.getAmount());
                break;

            case "Pay":
                player.pay(space.getAmount());
                break;


            case "Collect-Life":
                if (player.isLifeInsurance()) {
                    player.collect(space.getAmount());
                }
                break;

            case "Collect-Stock":
                if (player.hasStockInsurance()) {
                    player.collect(space.getAmount());
                }
                break;

            case "Pay-Car":
                if (!player.hasAutoInsurance()) {
                    player.pay(space.getAmount());
                }
                break;

            case "Pay-Fire":
                if (!player.hasFireInsurance()) {
                    player.pay(space.getAmount());
                }
                break;

            case "Pay-Stock":
                if (player.hasStockInsurance()) {
                    player.pay(space.getAmount());
                }
                break;

            case "Wait-Turn":
                System.out.println(player.getName() + " loses next turn.");
                player.setSkipTurns(1);
                break;

            case "Child":
                player.addChild();
                System.out.println("Congratulations !!; " + player.getName() + " had a child!");
                break;

            case "Twins":
                player.addChild();
                player.addChild();
                System.out.println("Congratulations !!; " + player.getName() + " had twins!");
                break;

            case "Retire":
                player.setRetired(true);
                break;

            case "Revenge":
                System.out.println("Revenge logic pending...");
                break;

            case "Spin-Again":
                System.out.println(player.getName() + " gets another spin!");
                break;

            case "Collect-Spin-3": //If you spin 3 collect $3000
            case "Collect-Spin-8": //If you spin 3 collect $8000
            case "Collect-Spin-the-Wheel":
                System.out.println("Spin reward logic pending...");
                break;

            case "Detour":
                System.out.println("Detour logic pending...");
                break;
        }

        dashboard.refresh(List.of(player));
    }
}
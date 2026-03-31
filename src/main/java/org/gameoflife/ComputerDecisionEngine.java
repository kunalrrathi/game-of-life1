package org.gameoflife;

import java.util.List;
import java.util.Random;

public class ComputerDecisionEngine {

    public boolean shouldBuyInsurance(Player player) {
        return player.getCash() > 50000;
    }

    public boolean shouldInvestInMarket(Player player) {
        return player.getCash() > 30000;
    }

    public int chooseBranch(List<Integer> options) {
        return options.get(new Random().nextInt(options.size()));
    }

    public boolean shouldMarry() {
        return true; // Always marry for now
    }

    public boolean chooseUniversityPath(Player player) {

        // 🎯 Simple logic for now

        // If player has low salary → go university
        if (player.getSalary() < 5000) {
            return true;
        }

        // If player has good cash → take shortcut
        if (player.getCash() > 100000) {
            return false;
        }

        // Otherwise random
        return new Random().nextBoolean();
    }
}
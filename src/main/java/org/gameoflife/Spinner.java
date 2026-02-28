package org.gameoflife;

import java.util.Random;

public class Spinner {

    private final Random random = new Random();

    public int spin() {
        return random.nextInt(10) + 1;  // 1 to 10
    }
}
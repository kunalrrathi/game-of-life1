package org.gameoflife;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class BoardLoader {

    public static List<BoardSpace> loadBoard(String filePath) {

        List<BoardSpace> spaces = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;

            // Skip header
            br.readLine();

            while ((line = br.readLine()) != null) {

                String[] t = line.split(",");

                int index = Integer.parseInt(t[0]);
                double x = Double.parseDouble(t[1]);
                double y = Double.parseDouble(t[2]);

                String color = t[3];
                String action = t[4];

                int amount = 0;
                if (!t[5].isEmpty()) {
                    amount = Integer.parseInt(t[5]);
                }

                String path = t[6];

                Integer next = null;
                if (!t[7].isEmpty()) {
                    next = Integer.parseInt(t[7]);
                }

                Integer branch = null;
                if (t.length > 8 && !t[8].isEmpty()) {
                    branch = Integer.parseInt(t[8]);
                }

                BoardSpace space = new BoardSpace(
                        index,
                        x,
                        y,
                        color,
                        action,
                        amount,
                        path,
                        next,
                        branch
                );

                spaces.add(space);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return spaces;
    }

}
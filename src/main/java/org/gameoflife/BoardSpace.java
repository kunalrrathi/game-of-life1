package org.gameoflife;

public class BoardSpace {

    private int index;

    private double x;
    private double y;

    private String color;
    private String action;

    private int amount;

    private String path;

    private Integer next;
    private Integer branch;

    private String spaceType; // Main, Shortcut, Split, Merge
    private int nextIndex;

    public BoardSpace(
            int index,
            double x,
            double y,
            String color,
            String action,
            int amount,
            String spaceType,
            Integer nextIndex,
            Integer branch
    ) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.color = color;
        this.action = action;
        this.amount = amount;
        this.branch = branch;
        this.spaceType = spaceType;
        this.nextIndex = nextIndex;
    }

    public int getIndex() {
        return index;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getColor() {
        return color;
    }

    public String getAction() {
        return action == null ? null : action.trim();
    }

    public int getAmount() {
        return amount;
    }

    public Integer getBranch() {
        return branch;
    }

    public boolean isRedSpace() {
        return "Red".equalsIgnoreCase(color);
    }

    public boolean hasBranch() {
        return branch != null;
    }

    public boolean isSplit() {
        return "Split".equalsIgnoreCase(path);
    }

    public boolean isMerge() {
        return "Merge".equalsIgnoreCase(path);
    }

    public String getSpaceType() {
        return spaceType;
    }

    public int getNextIndex() {
        return nextIndex;
    }

}
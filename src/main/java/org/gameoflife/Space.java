package org.gameoflife;

public class Space {

    private int index;
    private SpaceType type;
    private boolean isStop;
    private double x;
    private double y;

    private Integer branchIndex; // nullable

    public Space(int index, SpaceType type, boolean isStop,
                 double x, double y, Integer branchIndex) {

        this.index = index;
        this.type = type;
        this.isStop = isStop;
        this.x = x;
        this.y = y;
        this.branchIndex = branchIndex;
    }

    public int getIndex() {
        return index;
    }

    public SpaceType getType() {
        return type;
    }

    public boolean isStop() {
        return isStop;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Integer getBranchIndex() {
        return branchIndex;
    }
}

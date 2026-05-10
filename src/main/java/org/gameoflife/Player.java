package org.gameoflife;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Player {

    private boolean isComputer;

    private String name;
    private Profession profession = Profession.NONE;
    private int salary = 0;
    private boolean universityRoute = false;

    private int position = 0;
    private Circle token;
    private Color color;

    private int cash;   // starting money

    private int promissoryNotes = 0;

    private static final int LOAN_AMOUNT = 20000;
    private static final int INTEREST_PER_NOTE = 1000;

    private boolean autoInsurance;
    private boolean fireInsurance;
    private boolean stockInsurance;
    private boolean lifeInsurance;

    private int children;
    private boolean married;

    private boolean isBankrupt = false;
    private boolean isRetired = false;


    private boolean hasStock;

    private int skipTurns = 0;

    public void setProfession(Profession profession) {
        this.profession = profession;
        this.salary = profession.getSalary();
    }

    public Profession getProfession() {
        return profession;
    }

    public String getProfessionDisplayName() {
        return profession.getDisplayName();
    }

    public int getSalary() {
        return salary;
    }

    public void setUniversityRoute(boolean universityRoute) {
        this.universityRoute = universityRoute;
    }

    public boolean isUniversityRoute() {
        return universityRoute;
    }

    public Player() {
        token = new Circle(20);
    }

    public int getPosition() {
        return position;
    }

    public void move(int steps, int maxPosition) {
        position += steps;

        if (position >= maxPosition) {
            position = maxPosition - 1; // stop at last tile
        }
    }

    public Circle getToken() {
        return token;
    }

    public void setPosition(int newPosition) {
        this.position = newPosition;
    }

    public int getCash() {
        return cash;
    }

    public void addCash(int amount) {
        cash += amount;
        System.out.println("Cash updated: " + cash);
    }

    public void deductCash(int amount) {
        cash -= amount;
        System.out.println("Cash updated: " + cash);
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public int getPromissoryNotes() {
        return promissoryNotes;
    }

    public void borrowMoney(int amount) {
        cash += amount;
        promissoryNotes++;
        System.out.println("Borrowed: " + amount + " | Promissory Notes: " + promissoryNotes);
    }

    public void repayOneNote() {
        if (promissoryNotes > 0 && cash >= 20000) {
            promissoryNotes--;
            cash -= 20000;
            System.out.println("Repaid 1 note. Remaining notes: " + promissoryNotes);
        } else {
            System.out.println("Cannot repay note.");
        }
    }

    public void payInterest() {

        if (isRetired) return;

        int totalInterest = promissoryNotes * INTEREST_PER_NOTE;

        if (totalInterest > 0) {
            pay(totalInterest);
            System.out.println("Paid interest: " + totalInterest);
        }
    }

    public void ensureCashAvailable(int amountNeeded) {
        while (cash < amountNeeded) {
            borrowMoney(LOAN_AMOUNT);
            System.out.println("Auto borrowed: " + LOAN_AMOUNT);
        }
    }

    // For paying money (e.g. taxes, fees) **********************************
    public void pay(int amount) {
        ensureCashAvailable(amount);
        cash -= amount;
        System.out.println(this.getName() + ": Paid: " + amount + " | Remaining Cash: " + cash);
    }

    // For collecting money (e.g. salary, collect cards) **********************************
    public void collect(int amount) {
        cash += amount;
        System.out.println(this.getName() + ": Collect: " + amount + " | Remaining Cash: " + cash);
    }

    public boolean canRepayLoan() {
        return promissoryNotes > 0 && cash >= LOAN_AMOUNT;
    }

    public void repayLoan() {
        if (promissoryNotes <= 0) {
            System.out.println("No loans to repay.");
            return;
        }

        if (cash < LOAN_AMOUNT) {
            System.out.println("Not enough cash to repay loan.");
            return;
        }

        cash -= LOAN_AMOUNT;
        promissoryNotes--;

        System.out.println("Repaid loan: " + LOAN_AMOUNT +
                " | Remaining Notes: " + promissoryNotes +
                " | Cash: " + cash);
    }

    public void settleLoansAtRetirement() {

        if (promissoryNotes == 0) {
            System.out.println("No loans to settle at retirement.");
            return;
        }

        int totalDue = promissoryNotes * LOAN_AMOUNT;

        System.out.println("Retirement settlement required: " + totalDue);

        // Ensure enough cash
        ensureCashAvailable(totalDue);

        cash -= totalDue;
        promissoryNotes = 0;

        System.out.println("All loans cleared at retirement. Cash: " + cash);
    }

    public Player(String name) {
        this.name = name;
        this.cash = 10000;
    }

    public String getName() {
        return name;
    }

    public boolean hasAutoInsurance() {
        return autoInsurance;
    }

    public void setAutoInsurance(boolean autoInsurance) {
        this.autoInsurance = autoInsurance;
    }

    public void setMarried(boolean b) {
        this.married = b;
    }

    public boolean isLifeInsurance() {
        return lifeInsurance;
    }

    public void setLifeInsurance(boolean lifeInsurance) {
        this.lifeInsurance = lifeInsurance;
    }

    public boolean hasFireInsurance() {
        return fireInsurance;
    }

    public void setFireInsurance(boolean fireInsurance) {
        this.fireInsurance = fireInsurance;
    }

    public boolean hasStockInsurance() {
        return stockInsurance;
    }

    public void setStockInsurance(boolean stockInsurance) {
        this.stockInsurance = stockInsurance;
    }

    public boolean hasStock() {
        return hasStock;
    }

    public void setHasStock(boolean hasStock) {
        this.hasStock = hasStock;
    }

    public boolean isMarried() {
        return married;
    }

    public int getChildren() {
        return children;
    }

    public void addChild() {
        children++;
    }

    public boolean isBankrupt() {
        return isBankrupt;
    }

    public void setBankrupt(boolean bankrupt) {
        isBankrupt = bankrupt;
    }

    public boolean isRetired() {
        return isRetired;
    }

    public void setRetired(boolean retired) {
        isRetired = retired;
    }

    public boolean hasInsurance(GameController.InsuranceType type) {

        switch (type) {
            case LIFE: return isLifeInsurance();
            case AUTO: return hasAutoInsurance();
            case FIRE: return hasFireInsurance();
            case STOCK: return hasStockInsurance();
        }
        return false;
    }

    public void setCash(int i) {
        this.cash = i;
    }

    public boolean isComputer() {
        return isComputer;
    }

    public void setComputer(boolean computer) {
        isComputer = computer;
    }

    public void setSkipTurns(int turns) {
        this.skipTurns = turns;
    }

    public int getSkipTurns() {
        return skipTurns;
    }

    public boolean shouldSkipTurn() {
        return skipTurns > 0;
    }

    public void reduceSkipTurn() {
        if (skipTurns > 0) {
            skipTurns--;
        }
    }

    public Color getColor() {
        return (Color) token.getFill();
    }
}

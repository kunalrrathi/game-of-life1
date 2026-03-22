package org.gameoflife;

import javafx.scene.shape.Circle;

public class Player {

    private String name;
    private Profession profession = Profession.NONE;
    private int salary = 0;
    private boolean universityRoute = false;

    private int position = 0;
    private Circle token;

    private int cash;   // starting money

    private int promissoryNotes = 0;

    private static final int LOAN_AMOUNT = 20000;
    private static final int INTEREST_PER_NOTE = 1000;

    private boolean retired = false;

    private boolean autoInsurance;
    private boolean fireInsurance;
    private boolean stockInsurance;
    private boolean lifeInsurance;

    private int children;
    private boolean married;

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
        this.salary = profession.getSalary();
    }

    public Profession getProfession() {
        return profession;
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

        if (retired) return;

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
        System.out.println("Paid: " + amount + " | Remaining Cash: " + cash);
    }

    // For collecting money (e.g. salary, collect cards) **********************************
    public void collect(int amount) {
        cash += amount;
        System.out.println("Collect: " + amount + " | Remaining Cash: " + cash);
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
}

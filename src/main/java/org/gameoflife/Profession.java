package org.gameoflife;

public enum Profession {

    BUSINESS(12000),
    DOCTOR(50000),
    LAWYER(50000),
    JOURNALIST(24000),
    TEACHER(20000),
    PHYSICIST(30000),
    UNIVERSITY_DEGREE(16000),
    NONE(16000);   // for before selection

    private final int salary;

    Profession(int salary) {
        this.salary = salary;
    }

    public int getSalary() {
        return salary;
    }
}
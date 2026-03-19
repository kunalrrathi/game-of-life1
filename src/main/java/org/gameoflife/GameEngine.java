//package org.gameoflife;
//
//import java.util.List;
//
//public class GameEngine {
//
//    private final Player player;
//    private final Board board;
//    private final Spinner spinner;
//
//    // ===============================
//    // UNIVERSITY STATE
//    // ===============================
//    private final List<SpaceType> universityBranch;
//    private int universityIndex = 0;
//    private boolean inUniversityMode = false;
//
//    // ===============================
//    // CONSTRUCTOR
//    // ===============================
//    public GameEngine(Board board, Player player) {
//        this.board = board;
//        this.player = player;
//        this.spinner = new Spinner();
//
//        this.universityBranch = List.of(
//                SpaceType.PROFESSION_DOCTOR,
//                SpaceType.PROFESSION_JOURNALIST,
//                SpaceType.PROFESSION_LAWYER,
//                SpaceType.PROFESSION_TEACHER,
//                SpaceType.PROFESSION_PHYSICIST,
//                SpaceType.UNIVERSITY_DEGREE
//        );
//    }
//
//    // ===============================
//    // BASIC GETTERS
//    // ===============================
//    public Player getPlayer() {
//        return player;
//    }
//
//    public Board getBoard() {
//        return board;
//    }
//
//    public boolean isInUniversityMode() {
//        return inUniversityMode;
//    }
//
//    public int spin() {
//        return spinner.spin();
//    }
//
//    public Space getCurrentSpace() {
//        return board.getSpace(player.getPosition());
//    }
//
//    // ===============================
//    // MOVEMENT
//    // ===============================
//    public Space moveOneStep() {
//        player.move(1, board.getTotalTiles());
//        return getCurrentSpace();
//    }
//
//    public void moveForward(int steps) {
//        player.move(steps, board.getTotalTiles());
//    }
//
//    // ===============================
//    // PASS LOGIC
//    // ===============================
//    public void handlePassEvent(Space space) {
//
//        if (space.getType() == SpaceType.PAYDAY) {
//
//            if (!player.isRetired()) {
//                player.addCash(player.getSalary());
//            }
//
//            player.payInterest();
//        }
//    }
//
//    // ===============================
//    // LAND LOGIC
//    // ===============================
//    public GameEvent handleLandEvent(Space space) {
//
//        switch (space.getType()) {
//
//            case TAX -> {
//                player.pay(5000);
//                return GameEvent.NONE;
//            }
//
//            case MARRIAGE -> {
//                return GameEvent.MARRIAGE_STOP;
//            }
//
//            case RETIRE -> {
//
//                if (player.isRetired()) {
//                    return GameEvent.NONE;
//                }
//
//                player.setRetired(true);
//                player.settleLoansAtRetirement();
//
//                return GameEvent.RETIREMENT;
//            }
//
//            case UNIVERSITY_ASSIGNMENT -> {
//                System.out.println("Landing on University Assignment space..." + " University Route: " + player.isUniversityRoute() + " Profession: " + player.getProfession());
//                if (player.isUniversityRoute()
//                        && player.getProfession() == Profession.NONE) {
//
//                    inUniversityMode = true;
//                    universityIndex = 0;
//
//                    return GameEvent.UNIVERSITY_ASSIGNMENT;
//                }
//
//                return GameEvent.NONE;
//            }
//
//            default -> {
//                return GameEvent.NONE;
//            }
//        }
//    }
//
//    // ===============================
//    // UNIVERSITY BRANCH LOGIC
//    // ===============================
//    public void universitySpin(int steps) {
//
//        if (!inUniversityMode) return;
//
//        universityIndex += steps - 1;
//
//        if (universityIndex >= universityBranch.size()) {
//            universityIndex = universityBranch.size() - 1;
//        }
//
//        SpaceType result = universityBranch.get(universityIndex);
//
//        switch (result) {
//
//            case PROFESSION_DOCTOR -> {
//                assignProfession(Profession.DOCTOR);
//                moveForward(6);
//            }
//
//            case PROFESSION_JOURNALIST -> {
//                assignProfession(Profession.JOURNALIST);
//                moveForward(5);
//            }
//
//            case PROFESSION_LAWYER -> {
//                assignProfession(Profession.LAWYER);
//                moveForward(4);
//            }
//
//            case PROFESSION_TEACHER -> {
//                assignProfession(Profession.TEACHER);
//                moveForward(3);
//            }
//
//            case PROFESSION_PHYSICIST -> {
//                assignProfession(Profession.PHYSICIST);
//                moveForward(2);
//            }
//
//            case UNIVERSITY_DEGREE -> {
//                assignProfession(Profession.UNIVERSITY_DEGREE);
//            }
//
//            default -> {}
//        }
//
//        inUniversityMode = false;
//    }
//
//    // ===============================
//    // PROFESSION ASSIGNMENT
//    // ===============================
//    public void assignProfession(Profession profession) {
//
//        player.setProfession(profession);
//        player.setSalary(profession.getSalary());
//
//        System.out.println(
//                "Assigned Profession: "
//                        + profession
//                        + " | Salary: "
//                        + profession.getSalary()
//        );
//    }
//
//    // ===============================
//    // ROUTE SELECTION
//    // ===============================
//    public void chooseBusinessRoute() {
//        player.setUniversityRoute(false);
//        assignProfession(Profession.BUSINESS);
//    }
//
//    public void chooseUniversityRoute() {
//        player.setUniversityRoute(true);
//        player.setProfession(Profession.NONE);
//        inUniversityMode = false;
//        universityIndex = 0;
//    }
//
//    // ===============================
//    // LOAN
//    // ===============================
//    public void repayLoan() {
//        player.repayLoan();
//    }
//
//    // ===============================
//    // RETIREMENT BONUSES
//    // ===============================
//    public void applyMillionaireEstatesBonus() {
//        player.addCash(50000);
//    }
//
//    public void applyCountrysideBonus() {
//        player.addCash(50000);
//    }
//}
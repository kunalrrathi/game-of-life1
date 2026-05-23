package org.gameoflife;

public class SpaceResolver {

    private final WhiteSpaceHandler whiteHandler;
    private final RedSpaceHandler redHandler;
    private final JumpSpaceHandler jumpHandler;
    private final StopSpaceHandler stopHandler;
    private final NormalSpaceHandler normalHandler;

    public SpaceResolver(
            WhiteSpaceHandler whiteHandler,
            RedSpaceHandler redHandler,
            JumpSpaceHandler jumpHandler,
            StopSpaceHandler stopHandler,
            NormalSpaceHandler normalHandler) {
        this.whiteHandler = whiteHandler;
        this.redHandler = redHandler;
        this.jumpHandler = jumpHandler;
        this.stopHandler = stopHandler;
        this.normalHandler = normalHandler;
    }

    public void resolve(
            Player player,
            BoardSpace space,
            boolean isLanding
    ) {

        String color = safe(space.getColor());
        String action = safe(space.getAction());

        if ("White".equalsIgnoreCase(color)) {
            whiteHandler.handle(player, space);
            return;
        }

        if ("Red".equalsIgnoreCase(color)) {
            redHandler.handle(player, space);
            return;
        }

        if ("Jump".equalsIgnoreCase(color)) {

            if (isLanding) {
                jumpHandler.handle(player, space);
            }
            return;
        }

        if ("Stop".equalsIgnoreCase(color)) {

            if (isLanding) {
                stopHandler.handle(player, space);
            }
            return;
        }

        if (isLanding) {
            normalHandler.handle(player, space, action);
//            handleNormal(player, space, action);
        }
    }

//    private void handleNormal(
//            Player player,
//            BoardSpace space,
//            String action
//    ) {
//
//        switch (action) {
//
//            case "Collect":
//                player.collect(space.getAmount());
//                break;
//
//            case "Pay":
//                player.pay(space.getAmount());
//                break;
//
//            case "PayDay":
//                player.collect(player.getSalary());
//                break;
//
//            case "Spin-Again":
//                break;
//
//            case "Wait-Turn":
//                break;
//        }
//    }

//    public void flush(Player player) {
//        whiteHandler.flush(player);
//    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
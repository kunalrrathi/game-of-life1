package org.gameoflife;

public class SpaceResolver {

    private final WhiteSpaceHandler whiteHandler;
    private final RedSpaceHandler redHandler;
    private final JumpSpaceHandler jumpHandler;
    private final StopSpaceHandler stopHandler;

    public SpaceResolver(
            WhiteSpaceHandler whiteHandler,
            RedSpaceHandler redHandler,
            JumpSpaceHandler jumpHandler,
            StopSpaceHandler stopHandler
    ) {
        this.whiteHandler = whiteHandler;
        this.redHandler = redHandler;
        this.jumpHandler = jumpHandler;
        this.stopHandler = stopHandler;
    }

    public void resolve(
            Player player,
            BoardSpace space,
            boolean isLanding
    ) {

        String color = safe(space.getColor());
        String action = safe(space.getAction());

        if ("Red".equalsIgnoreCase(color)) {

            redHandler.handle(player, space);
            return;
        }

        if ("White".equalsIgnoreCase(color)) {

            whiteHandler.collect(space);
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
            handleNormal(player, space, action);
        }
    }

    private void handleNormal(
            Player player,
            BoardSpace space,
            String action
    ) {

        switch (action) {

            case "Collect":
                player.collect(space.getAmount());
                break;

            case "Pay":
                player.pay(space.getAmount());
                break;

            case "PayDay":
                player.collect(player.getSalary());
                break;

            case "Spin-Again":
                break;

            case "Wait-Turn":
                break;
        }
    }

    public void flush(Player player) {
        whiteHandler.flush(player);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
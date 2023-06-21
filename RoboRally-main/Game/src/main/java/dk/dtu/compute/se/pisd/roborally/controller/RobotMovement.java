package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.exceptions.ImpossibleMoveException;
import dk.dtu.compute.se.pisd.roborally.model.*;

import static dk.dtu.compute.se.pisd.roborally.model.Command.AGAIN;

public class RobotMovement {

    private final GameController gameController;

    public RobotMovement(GameController gameController) {
        this.gameController = gameController;
    }

    public void moveForward(Player player, int moves) {
        for (int i = 0; i < moves; i++) {
            attemptMove(player);
        }
    }

    private void attemptMove(Player player) {
        try {
            Space target = findTargetSpace(player);
            pushPlayerInTargetSpace(target, player.getHeading());
            target.setPlayer(player);
        } catch (ImpossibleMoveException e) {
            // Do nothing for now...
        }
    }

    private Space findTargetSpace(Player player) throws ImpossibleMoveException {
        Heading heading = player.getHeading();
        Space target = gameController.board.getNeighbour(player.getSpace(), heading);
        if (target == null) {
            throw new ImpossibleMoveException(player, player.getSpace(), heading);
        }
        return target;
    }

    private void pushPlayerInTargetSpace(Space target, Heading heading) {
        if (isOccupied(target)) {
            Player playerBlocking = target.getPlayer();
            Heading originalHeading = playerBlocking.getHeading();
            playerBlocking.setHeading(heading);
            moveForward(playerBlocking, 1);
            playerBlocking.setHeading(originalHeading);
        }
    }

    private boolean isOccupied(Space space) {
        return gameController.board.getSpace(space.x, space.y).getPlayer() != null;
    }

    public void turnRight(Player player) {
        if (isPlayerOnBoard(player)) {
            player.setHeading(player.getHeading().next());
        }
    }

    public void turnLeft(Player player) {
        if (isPlayerOnBoard(player)) {
            player.setHeading(player.getHeading().prev());
        }
    }

    private boolean isPlayerOnBoard(Player player) {
        return player.board == gameController.board;
    }

    public void uTurn(Player player) {
        turnLeft(player);
        turnLeft(player);
    }

    public void moveBackward(Player player) {
        uTurn(player);
        moveForward(player, 1);
        uTurn(player);
    }

    public void again(Player player, int step) {
        if (step < 1) return;
        Command prevCommand = player.getProgramField(step - 1).getCard().command;
        if (prevCommand == AGAIN) {
            again(player, step - 1);
        } else {
            player.getProgramField(step).setCard(new CommandCard(prevCommand));
            gameController.executeNextStep();
            player.getProgramField(step).setCard(new CommandCard(AGAIN));
        }
    }
}

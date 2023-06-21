package dk.dtu.compute.se.pisd.roborally.controller.fieldaction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Space;

/**
 * @author Malaz ALZarrad
 */
public class RotatingGear extends FieldAction {

    public enum Direction {
        LEFT,
        RIGHT
    }

    private Direction direction;

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean doAction(GameController gameController, Space space) {
        return isFirstActionRotatingGear(space) && rotatePlayer(gameController, space);
    }

    private boolean isFirstActionRotatingGear(Space space) {
        return !space.getActions().isEmpty() && space.getActions().get(0) instanceof RotatingGear;
    }

    private boolean rotatePlayer(GameController gameController, Space space) {
        RotatingGear gear = (RotatingGear) space.getActions().get(0);
        Direction gearDirection = gear.getDirection();

        if (gearDirection == Direction.LEFT) {
            gameController.movementController.turnLeft(space.getPlayer());
        } else {
            gameController.movementController.turnRight(space.getPlayer());
        }

        return true;
    }
}

package dk.dtu.compute.se.pisd.roborally.controller.fieldaction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;


/**
 * @author Malaz ALZarrad
 */
public class Pit extends FieldAction {

    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player player = space.getPlayer();
        return player != null;
    }
}


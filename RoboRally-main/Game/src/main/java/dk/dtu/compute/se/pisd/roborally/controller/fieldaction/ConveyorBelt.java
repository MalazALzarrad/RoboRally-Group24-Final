/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.controller.fieldaction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

/**
 * Controls the conveyor belt and represents both types of conveyor belts
 *
 * @author Ekkart Kindler, ekki@dtu.dkm,
 */
public class ConveyorBelt extends FieldAction {

    private Heading heading;
    private int numberOfMoves;

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }

    public void setNumberOfMoves(int numberOfMoves) {
        this.numberOfMoves = numberOfMoves;
    }
    public int getNumberOfMoves() {
        return numberOfMoves;
    }

    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        if (space.getActions().isEmpty()) {
            return false;
        }

        FieldAction action = space.getActions().get(0);
        if (!(action instanceof ConveyorBelt)) {
            return false;
        }

        ConveyorBelt conveyorBelt = (ConveyorBelt) action;
        Player player = space.getPlayer();
        if (player == null) {
            return false;
        }

        Heading initialHeading = player.getHeading();
        player.setHeading(conveyorBelt.heading);

        for (int i = 1; i <= conveyorBelt.numberOfMoves; i++) {
            if (i > 1 && player.getSpace().getActions().size() > 0 && player.getSpace().getActions().get(0) instanceof ConveyorBelt nextBelt) {
                player.setHeading(nextBelt.heading);
            }

            Space neighbour = gameController.board.getNeighbour(player.getSpace(), player.getHeading());
            if (neighbour != null && neighbour.getPlayer() != null) {
                player.setHeading(initialHeading);
                return false;
            }

            gameController.movementController.moveForward(player, 1);
        }

        player.setHeading(initialHeading);

        return true;
    }
}

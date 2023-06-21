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
package dk.dtu.compute.se.pisd.roborally.view;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.*;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Creates the StackPane for each space and clears the player from the space after each move
 *
 * @author Ekkart Kindler, ekki@dtu.dk, Mikael Fangel
 */
public class SpaceView extends StackPane implements ViewObserver {

    public final Space space;

    public SpaceView(@NotNull Space space) {
        this.space = space;
        setupSpace();
        handleSpaceActions();
        addWallImages();
        space.attach(this);
        update(space);
    }

    private void setupSpace() {
        this.setId("space");
        this.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/space.css")).toExternalForm());
        ImageView bg = new ImageView(new Image("space.png"));
        this.getChildren().add(bg);
    }

    private void handleSpaceActions() {
        if (space.getActions().isEmpty()) {
            return;
        }
        ImageView imageView;
        Object action = space.getActions().get(0);
        if (action instanceof ConveyorBelt conveyorBelt) {
            imageView = handleConveyorBeltAction(conveyorBelt);
        } else if (action instanceof RotatingGear rotatingGear) {
            imageView = handleRotatingGearAction(rotatingGear);
        } else if (action instanceof Pit) {
            imageView = new ImageView(new Image("pit.png"));
        } else if (action instanceof Checkpoint checkpoint) {
            imageView = handleCheckpointAction(checkpoint);
        } else if (action instanceof StartGear) {
            imageView = new ImageView(new Image("startingGear.png"));
        }
        else {
            imageView = new ImageView(new Image("space.png"));
        }
        this.getChildren().add(imageView);
    }

    private ImageView handleConveyorBeltAction(ConveyorBelt conveyorBelt) {
        Image conBelt = conveyorBelt.getNumberOfMoves() <= 1 ? new Image("conveyorBelt.png") : new Image("conveyorBeltBlue.png");
        ImageView imageView = new ImageView(conBelt);
        imageView.setRotate((90 * conveyorBelt.getHeading().ordinal()) % 360);
        return imageView;
    }

    private ImageView handleRotatingGearAction(RotatingGear rotatingGear) {
        return new ImageView(new Image(rotatingGear.getDirection() == RotatingGear.Direction.RIGHT ? "rotatingGearRight.png" : "rotatingGearLeft.png"));
    }

    private ImageView handleCheckpointAction(Checkpoint checkpoint) {
        return switch (checkpoint.getCheckpointNumber()) {
            case 1 -> new ImageView(new Image("checkPoint1.png"));
            case 2 -> new ImageView(new Image("checkPoint2.png"));
            case 3 -> new ImageView(new Image("checkPoint3.png"));
            default -> new ImageView(new Image("space.png"));
        };
    }

    private void addWallImages() {
        for (Heading wall : space.getWalls()) {
            ImageView wallImage = new ImageView(new Image("wall.png"));
            wallImage.setRotate((90 * wall.ordinal()) % 360);
            this.getChildren().add(wallImage);
        }
    }

    private void updatePlayer() {
        // To update player position. Should be programmed more defensively
        for (int i = 0; i < this.getChildren().size(); i++) {
            if (this.getChildren().get(i).getClass().getSimpleName().equals("Polygon")) {
                this.getChildren().remove(i);
            }
        }

        Player player = space.getPlayer();
        if (player != null) {
            //Creates a new instance of Polygon.
            //Params:
            //points – the coordinates of the polygon
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0);
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90 * player.getHeading().ordinal()) % 360);
            this.getChildren().add(arrow);
        }
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            updatePlayer();
        }
    }

}

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
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import javafx.scene.control.TabPane;

/**
 * Shows all the player tabs
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class PlayersView extends TabPane implements ViewObserver {

    private final Board board;
    private final GameController gameController;

    private PlayerView[] playerViews;

    public PlayersView(GameController gameController) {
        this.gameController = gameController;
        this.board = gameController.board;

        initializePlayerViews();
    }

    private void initializePlayerViews() {
        this.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        int numberOfPlayers = board.getPlayersNumber();
        playerViews = new PlayerView[numberOfPlayers];

        for (int i = 0; i < numberOfPlayers; i++) {
            createAndAddPlayerView(i);
        }

        board.attach(this);
        update(board);
    }

    private void createAndAddPlayerView(int playerIndex) {
        if (gameController.client != null && gameController.getPlayerNumber() == playerIndex || gameController.client == null) {
            playerViews[playerIndex] = new PlayerView(gameController, board.getPlayer(playerIndex));
            this.getTabs().add(playerViews[playerIndex]);
        }
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == board) {
            this.getSelectionModel().select(board.getPlayerNumber(board.getCurrentPlayer()));
        }
    }

    public PlayerView[] getPlayerViews() {
        return playerViews;
    }
}

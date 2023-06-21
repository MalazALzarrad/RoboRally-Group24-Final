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
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.httpclient.Client;
import dk.dtu.compute.se.pisd.httpclient.GameListView;
import dk.dtu.compute.se.pisd.roborally.RoboRally;
import dk.dtu.compute.se.pisd.roborally.exceptions.TheBoardCantFoundException;
import dk.dtu.compute.se.pisd.roborally.fileaccess.ReadWriteGame;
import dk.dtu.compute.se.pisd.roborally.fileaccess.SerializeState;
import dk.dtu.compute.se.pisd.roborally.model.Board;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Controls the application before the game is started
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class AppController implements Observer {
    private static final List<Integer> PLAYER_NUMBER_OPTIONS = List.of(2, 3, 4, 5, 6);
    private final RoboRally roboRally;
    private GameController gameController;
    private final Client client;
    private boolean serverClientMode;
    private final GameListView slv;

    public AppController(@NotNull RoboRally roboRally) {
        this.roboRally = roboRally;
        this.client = new Client();
        this.slv = new GameListView(client, this);
    }

    public void newGame() {
        String title = "Player Number";
        String header = "Select number of players";
        Optional<Integer> numPlayers = roboRally.getPopupBoxes().promptForPlayersNumber(title, header, PLAYER_NUMBER_OPTIONS);

        if (numPlayers.isEmpty()) {
            client.leaveTheGame();
            return;
        }
        if (gameController != null && !stopGame()) return;
        createNewGame(numPlayers.get(), false);
    }

    private void createNewGame(int numPlayers, boolean prevFailed) {
        String title = "CHOOSE BOARD";
        String header = "Select which board to play";
        Optional<String> chosenBoard = roboRally.getPopupBoxes().promptForBoardName(title, header, ReadWriteGame.getNamesOfDefaultBoard());

        if (chosenBoard.isEmpty()) {
            client.leaveTheGame();
            return;
        }
        try {
            Board board = LoadAndSaveTheGame.newBoard(numPlayers, chosenBoard.get());
            GameControllersSetup(board);
            if (client.hasServerConnection())
                client.updateTheGame(SerializeState.serializeGame(board));
        } catch (TheBoardCantFoundException e) {
            createNewGame(numPlayers, true);
        }
    }

    public void saveGame() {
        String dialog = roboRally.getPopupBoxes().getInputString("SAVE GAME", "Enter a Save game name");
        if (dialog != null)
            LoadAndSaveTheGame.saveBoardToDisk(gameController.board, dialog);
    }

    public void loadGame() {
        if (gameController != null) return;
        createLoadedGame();
    }

    private void createLoadedGame() {
        String title = "CHOOSE BOARD";
        String header = "Select which board to play";
        Optional<String> chosenBoard = roboRally.getPopupBoxes().promptForBoardName(title, header, ReadWriteGame.getNamesOfSavedBoards());

        if (chosenBoard.isEmpty()) return;

        try {
            Board board = LoadAndSaveTheGame.loadBoard(chosenBoard.get());
            GameControllersSetup(board);
        } catch (TheBoardCantFoundException e) {
            createLoadedGame();
        }
    }

    public void hostGame(String... errorMessage) {
        String title = "Start game server";
        String header = "Server name:";
        if (errorMessage.length > 0) {
            header = errorMessage[0] + "\ntry again";
        }
        String result = roboRally.getPopupBoxes().getInputString(title, header);
        if (result == null) return;
        String response = client.hostOfTheGame(result);
        if (!"success".equals(response)) {
            hostGame(response);
            return;
        }
        serverClientMode = true;
        title = "Start new or continue?:";
        header = "Start new or continue?:";
        List<String> options = List.of("new game", "load game");
        Optional<String> out = roboRally.getPopupBoxes().promptForBoardName(title, header, options);
        if (out.isPresent() && "load game".equals(out.get())) {
            loadGame();
        } else {
            newGame();
        }
    }


    public void joinGame(String id) {
        String message = client.joinTheGame(id);
        if (!"ok".equals(message)) {
            roboRally.getPopupBoxes().alertBox("Error", message);
            return;
        }
        serverClientMode = true;
        Board board = SerializeState.deserialiseTheGame(client.getStateOfTheGame(), true);
        GameControllersSetup(board);
        gameController.setPlayerNumber(client.getNumberOfRobot());
    }

    public void connectToServer() {
        String serverList = client.listOfTheGames();
        if ("server timeout".equals(serverList)) {
            roboRally.getPopupBoxes().alertBox("error", serverList);
            return;
        }
        slv.addTheServer(serverList);
        slv.viewTheTable();
    }

    public void disconnectFromServer() {
        client.leaveTheGame();
    }

    private void GameControllersSetup(Board board) {
        gameController = new GameController(this, Objects.requireNonNull(board), serverClientMode ? client : null);
        board.setCurrentPlayer(board.getPlayer(0));
        gameController.startProgrammingPhase();
        roboRally.createBoardView(gameController);
    }

    public boolean stopGame() {
        if (gameController == null) return false;

        gameController = null;
        roboRally.createBoardView(null);
        return true;
    }

    public void exit() {
        if (gameController != null) {
            Optional<ButtonType> result = roboRally.getPopupBoxes().alertBox("Exit RoboRally?", "Are you sure you want to exit RoboRally?");
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        if (gameController == null || stopGame()) {
            client.leaveTheGame();
            Platform.exit();
        }
    }

    public boolean isGameRunning() {
        return gameController != null;
    }

    @Override
    public void update(Subject subject) {
        // XXX do nothing for now
    }

    public RoboRally getRoboRally() {
        return roboRally;
    }
}

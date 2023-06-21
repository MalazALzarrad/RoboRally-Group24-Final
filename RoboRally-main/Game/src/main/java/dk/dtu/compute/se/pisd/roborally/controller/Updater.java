package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.httpclient.Client;
import dk.dtu.compute.se.pisd.roborally.fileaccess.SerializeState;
import javafx.application.Platform;

import java.util.TimerTask;


public class Updater extends TimerTask {
    private static GameController gameController;
    private static AppController appController;
    private static boolean update = true;
    private static Client client;

    public void run() {
        if (shouldUpdate()) {
            gameController.refreshUpdater();
            if (!isGameOver()) {
                String jsonBoard = fetchGameState();
                if (isGameStateValid(jsonBoard)) {
                    updateGameBoard(jsonBoard);
                    notifyUIOfUpdate();
                } else {
                    stopTask();
                }
            }
        }
    }

    private boolean shouldUpdate() {
        return update;
    }

    private boolean isGameOver() {
        return gameController.board.gameOver;
    }

    private String fetchGameState() {
        return client.getStateOfTheGame();
    }

    private boolean isGameStateValid(String gameState) {
        return !gameState.contains("error");
    }

    private void updateGameBoard(String jsonBoard) {
        gameController.board = SerializeState.deserialiseTheGame(jsonBoard, true);
    }

    private void notifyUIOfUpdate() {
        Platform.runLater(this::updateBoard);
    }

    private void stopTask() {
        cancel();
    }


    /**
     * Generates a new board view when board is changed
     */
    public void updateBoard() {
        createBoardViewInRoboRally();
    }

    private void createBoardViewInRoboRally() {
        appController.getRoboRally().createBoardView(gameController);
    }

    public static void setClient(Client incomingClient) {
        client = incomingClient;
    }

    public static void setGameController(GameController incomingGameController) {
        gameController = incomingGameController;
    }

    public static boolean getUpdate() {
        return update;
    }

    public static void setUpdate(boolean incomingUpdate) {
        update = incomingUpdate;
    }

    public static void setAppController(AppController incomingAppController) {
        appController = incomingAppController;
    }
}
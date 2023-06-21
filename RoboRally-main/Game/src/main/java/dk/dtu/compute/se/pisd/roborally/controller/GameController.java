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

import dk.dtu.compute.se.pisd.httpclient.Client;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.*;
import dk.dtu.compute.se.pisd.roborally.fileaccess.SerializeState;
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Handles all the game logic for RoboRally
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class GameController {
    public Board board;
    private int playerNum; // Given from the server
    final public RobotMovement movementController;
    final private AppController appController;
    public final Client client;
    private boolean skipProgrammingPhase = true;

    public GameController(AppController appController, @NotNull Board board, Client client) {
        this.appController = appController;
        this.board = board;
        this.client = client;
        movementController = new RobotMovement(this);

        if (isClientNonNull(client)) {
            updateClientAndStartUpdater();
        }
    }

    private boolean isClientNonNull(Client client) {
        return client != null;
    }

    private void updateClientAndStartUpdater() {
        client.updateTheGame(SerializeState.serializeGame(board));
        playerNum = client.getNumberOfRobot();
        setControllersAndStartUpdater();
    }

    private void setControllersAndStartUpdater() {
        Updater.setGameController(this);
        Updater.setAppController(appController);
        Updater.setClient(client);
        new Timer().schedule(new Updater(), 0, 1000);
    }

    public void moveCurrentPlayerToSpace(@NotNull Space space) {
        if (isCurrentBoard(space)) {
            Player currentPlayer = board.getCurrentPlayer();
            movePlayerIfValid(currentPlayer, space);
        }
    }

    private boolean isCurrentBoard(Space space) {
        return space.board == board;
    }

    private void movePlayerIfValid(Player player, Space space) {
        if (player != null && space.getPlayer() == null) {
            performMoveAndAction(player, space);
            switchToNextPlayer(player);
        }
    }

    private void performMoveAndAction(Player player, Space space) {
        player.setSpace(space);
        if (space.getActions().size() > 0) {
            FieldAction action = space.getActions().get(0);
            action.doAction(this, space);
        }
    }

    private void switchToNextPlayer(Player currentPlayer) {
        int playerNumber = (board.getPlayerNumber(currentPlayer) + 1) % board.getPlayersNumber();
        board.setCurrentPlayer(board.getPlayer(playerNumber));
    }


    public void startProgrammingPhase() {
        refreshUpdater();

        if (isNewlyLoadedDefaultBoard() || !skipProgrammingPhase) {
            prepareBoardForProgramming();
            setCommandsForPlayers();
        } else {
            skipProgrammingPhase = false;
        }
    }

    private boolean isNewlyLoadedDefaultBoard() {
        return LoadAndSaveTheGame.getBoardCreated();
    }

    private void prepareBoardForProgramming() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    private void setCommandsForPlayers() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            player.setRegistersDisabled(false);

            if (player != null) {
                resetPlayerProgramFields(player);
                assignCardsToPlayer(player);
            }
        }
    }

    private void resetPlayerProgramFields(Player player) {
        for (int j = 0; j < Player.NO_REGISTERS; j++) {
            CommandCardField field = player.getProgramField(j);
            field.setCard(null);
            field.setVisible(true);
        }
    }

    private void assignCardsToPlayer(Player player) {
        for (int j = 0; j < Player.NO_CARDS; j++) {
            CommandCardField field = player.getCardField(j);
            field.setCard(generateRandomCommandCard());
            field.setVisible(true);
        }
    }



    public CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        ArrayList<Command> commandList = new ArrayList<>(Arrays.asList(commands).subList(0, 9));
        int randomIndex = getRandomIndex(commandList.size());
        return new CommandCard(commandList.get(randomIndex));
    }

    private int getRandomIndex(int size) {
        return (int) (Math.random() * size);
    }

    public void finishProgrammingPhase() {
        if (isLastPlayerOrClientNull()) {
            proceedToActivationPhase();
        } else if (client != null) {
            changePlayer(board.getCurrentPlayer(), board.step);
        }
    }

    private boolean isLastPlayerOrClientNull() {
        return board.getPlayerNumber(board.getCurrentPlayer()) == board.getPlayers().size() - 1 ||
                client == null;
    }

    private void proceedToActivationPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        assertPlayerPriorityAndChangeBoardPlayers();

        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        if (client != null) {
            refreshUpdater();
            pushGameState();
        }
    }


    private void makeProgramFieldsVisible(int register) {
        if (isValidRegister(register)) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                makePlayerProgramFieldVisible(i, register);
            }
        }
    }

    private boolean isValidRegister(int register) {
        return register >= 0 && register < Player.NO_REGISTERS;
    }

    private void makePlayerProgramFieldVisible(int playerIndex, int register) {
        Player player = board.getPlayer(playerIndex);
        CommandCardField field = player.getProgramField(register);
        field.setVisible(true);
    }

    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            makePlayerProgramFieldsInvisible(i);
        }
    }

    private void makePlayerProgramFieldsInvisible(int playerIndex) {
        Player player = board.getPlayer(playerIndex);
        for (int j = 0; j < Player.NO_REGISTERS; j++) {
            CommandCardField field = player.getProgramField(j);
            field.setVisible(false);
        }
    }

    public void executePrograms() {
        setStepMode(false);
        continuePrograms();
    }

    public void executeStep() {
        setStepMode(true);
        continuePrograms();
    }

    private void setStepMode(boolean stepMode) {
        board.setStepMode(stepMode);
    }



    public void executeCommandAndResumeActivation(Command command) {
        setPhase(Phase.ACTIVATION);

        Player currentPlayer = getCurrentPlayer();
        executeCommand(currentPlayer, command);
        changePlayer(currentPlayer, board.getStep());
    }

    private void setPhase(Phase phase) {
        board.setPhase(phase);
    }

    private Player getCurrentPlayer() {
        return board.getCurrentPlayer();
    }

    private void continuePrograms() {
        while (canContinuePrograms()) {
            executeNextStep();
        }
    }

    private boolean canContinuePrograms() {
        return board.getPhase() == Phase.ACTIVATION && !board.isStepMode();
    }

    protected void executeNextStep() {
        Player currentPlayer = getCurrentPlayer();
        if (isActivationPhaseAndPlayerExists(currentPlayer)) {
            int step = board.getStep();
            if (isValidStep(step)) {
                executeStepIfPossible(currentPlayer, step);
                if (isActivationPhase()) {
                    changePlayer(currentPlayer, step);
                }
            } else {
                assert false; // this should not happen
            }
        } else {
            assert false; // this should not happen
        }
    }

    private boolean isActivationPhaseAndPlayerExists(Player player) {
        return board.getPhase() == Phase.ACTIVATION && player != null;
    }

    private boolean isValidStep(int step) {
        return step >= 0 && step < Player.NO_REGISTERS;
    }

    private void executeStepIfPossible(Player player, int step) {
        CommandCard card = player.getProgramField(step).getCard();
        if (card != null && !player.isRegistersDisabled()) {
            Command command = card.command;
            executeCommand(player, command);
        }
    }

    private boolean isActivationPhase() {
        return board.getPhase() == Phase.ACTIVATION;
    }

    /**
     * Ends the current game and stops the gui
     *
     * @author Malaz ALZarrad
     */
    public void endGame() {
        disconnectFromServer();
        stopGame();
    }

    private void disconnectFromServer() {
        Platform.runLater(appController::disconnectFromServer);
    }

    private void stopGame() {
        Platform.runLater(appController::stopGame);
    }

    public void assertPlayerPriorityAndChangeBoardPlayers() {
        rotatePlayers();
        setFirstPlayerAsCurrent();

        if (appControllerExists()) {
            recreatePlayersView();
        }
    }

    private void rotatePlayers() {
        List<Player> players = board.getPlayers();
        Player firstPlayer = players.remove(0);
        players.add(firstPlayer);

        board.setPlayers(players);
    }

    private void setFirstPlayerAsCurrent() {
        board.setCurrentPlayer(board.getPlayers().get(0));
    }

    private boolean appControllerExists() {
        return appController != null;
    }

    private void changePlayer(Player currentPlayer, int step) {
        int nextPlayerNumber = getNextPlayerNumber(currentPlayer);

        if (nextPlayerNumber < getPlayersNumber()) {
            setCurrentPlayer(board.getPlayer(nextPlayerNumber));
        } else {
            incrementStepAndActivateBoardElements(++step);
            if (isStepValid(step)) {
                setStepAndFirstPlayerAsCurrent(step);
            } else {
                startProgrammingPhase();
            }
        }

        pushGameState();
        refreshUpdater();
    }

    private int getNextPlayerNumber(Player player) {
        return board.getPlayerNumber(player) + 1;
    }

    private int getPlayersNumber() {
        return board.getPlayersNumber();
    }

    private void setCurrentPlayer(Player player) {
        board.setCurrentPlayer(player);
    }

    private void incrementStepAndActivateBoardElements(int step) {
        boardElementsActivationOrder();
        if (step < Player.NO_REGISTERS) {
            makeProgramFieldsVisible(step);
        }
    }

    private boolean isStepValid(int step) {
        return step < Player.NO_REGISTERS;
    }

    private void setStepAndFirstPlayerAsCurrent(int step) {
        board.setStep(step);
        setCurrentPlayer(board.getPlayer(0));
    }


    private void executeCommand(@NotNull Player player, Command command) {
        if (isPlayerOnBoard(player) && command != null) {
            executeBasedOnCommand(player, command);

            if (clientExists())
                client.updateTheGame(SerializeState.serializeGame(board));
        }
    }

    private boolean isPlayerOnBoard(Player player) {
        return player.board == board;
    }

    private void executeBasedOnCommand(Player player, Command command) {
        // Switching over the command and executing accordingly
        switch (command) {
            case MOVE1:
                movementController.moveForward(player, 1);
                break;
            case MOVE2:
                movementController.moveForward(player, 2);
                break;
            case MOVE3:
                movementController.moveForward(player, 3);
                break;
            case RIGHT:
                movementController.turnRight(player);
                break;
            case LEFT:
                movementController.turnLeft(player);
                break;
            case OPTION_LEFT_RIGHT:
                board.setPhase(Phase.PLAYER_INTERACTION);
                break;
            case UTURN:
                movementController.uTurn(player);
                break;
            case MOVEBACK:
                movementController.moveBackward(player);
                break;
            case AGAIN:
                movementController.again(player, board.getStep());
                break;

            default:
                // DO NOTHING (for now)
                break;
        }
    }

    private boolean clientExists() {
        return client != null;
    }

    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        if (canCardsBeMoved(source, target)) {
            moveSourceCardToTarget(source, target);
            return true;
        }
        return false;
    }

    private boolean canCardsBeMoved(CommandCardField source, CommandCardField target) {
        return source.getCard() != null && target.getCard() == null;
    }

    private void moveSourceCardToTarget(CommandCardField source, CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        target.setCard(sourceCard);
        source.setCard(null);
    }


    public void recreatePlayersView() {
        appController.getRoboRally().getBoardView().updatePlayersView();
    }

    /**
     * Executes the board space in the order:
     * 1. ConveyorBelts
     * 2. Gears
     * 3. Pits
     * 4. Checkpoints
     * <p>
     * And while executing the order of spaces it ensures that robot movement rules for conveyor belts is held
     *
     * @author Malaz ALZarrad
     */
    private void boardElementsActivationOrder() {
        List<Player> players = board.getPlayers();
        activateConveyorBelts(players);
        activateRotatingGears(players);
        activatePits(players);
        activateCheckpoints(players);
    }

    private void activateConveyorBelts(List<Player> players) {
        ArrayDeque<Player> actionsToBeHandled = new ArrayDeque<>(board.getPlayersNumber());
        for (int i = 2; i > 0; i--) {
            for (Player player : players) {
                if (isConveyorBeltActionWithMoves(player, i)) {
                    actionsToBeHandled.add(player);
                }
            }
            handlePlayerActions(actionsToBeHandled);
        }
    }
    private boolean isConveyorBeltActionWithMoves(Player player, int movesNumber) {
        return !player.getSpace().getActions().isEmpty()
                && player.getSpace().getActions().get(0) instanceof ConveyorBelt spaceBelt
                && spaceBelt.getNumberOfMoves() == movesNumber;
    }

    private void handlePlayerActions(ArrayDeque<Player> actionsToBeHandled) {
        int playersInQueue;
        int j = 0;
        while (!actionsToBeHandled.isEmpty()) {
            playersInQueue = actionsToBeHandled.size();
            Player currentPlayer = actionsToBeHandled.pop();
            Space startLocation = currentPlayer.getSpace();
            if (!executePlayerAction(currentPlayer)) {
                currentPlayer.setSpace(startLocation);
                actionsToBeHandled.add(currentPlayer);
            }
            j++;
            if (j == playersInQueue && playersInQueue == actionsToBeHandled.size()) {
                actionsToBeHandled.clear();
                break;
            }
        }
    }

    private boolean executePlayerAction(Player player) {
        return player.getSpace().getActions().get(0).doAction(this, player.getSpace());
    }

    private void activateRotatingGears(List<Player> players) {
        for (Player player : players) {
            if (isInstanceOfAction(player, RotatingGear.class)) {
                executePlayerAction(player);
            }
        }
    }

    private void activatePits(List<Player> players) {
        for (Player player : players) {
            if (isInstanceOfAction(player, Pit.class)) {
                executePlayerAction(player);
            }
        }
    }

    private void activateCheckpoints(List<Player> players) {
        for (Player player : players) {
            if (isInstanceOfAction(player, Checkpoint.class)) {
                executePlayerAction(player);
            }
        }
    }

    private boolean isInstanceOfAction(Player player, Class<?> actionClass) {
        return !player.getSpace().getActions().isEmpty()
                && player.getSpace().getActions().get(0).getClass().isAssignableFrom(actionClass);
    }


    public void refreshUpdater() {
        if (client != null) {
            Updater.setUpdate(isMyTurn());

            if (board.gameOver) {
                endGame();
            }
        }

        if (board.gameOver) {
            stopUpdater();
        }
    }

    private void stopUpdater() {
        Updater.setUpdate(false);
    }

    public void pushGameState() {
        if (isClientConnected()) {
            client.updateTheGame(serializeGame());
        }
    }

    private boolean isClientConnected() {
        return client != null;
    }

    private String serializeGame() {
        return SerializeState.serializeGame(board);
    }


    public boolean isMyTurn() {
        return !isCurrentPlayer() && isClientConnected();
    }

    private boolean isCurrentPlayer() {
        return board.getCurrentPlayer() == board.getPlayer(playerNum);
    }

    public void setPlayerNumber(int num) {
        playerNum = num;
    }

    public int getPlayerNumber() {
        return playerNum;
    }


}
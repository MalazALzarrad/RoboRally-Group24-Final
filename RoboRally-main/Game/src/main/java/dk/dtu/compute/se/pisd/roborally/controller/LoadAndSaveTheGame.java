package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.StartGear;
import dk.dtu.compute.se.pisd.roborally.exceptions.TheBoardCantFoundException;
import dk.dtu.compute.se.pisd.roborally.fileaccess.ReadWriteGame;
import dk.dtu.compute.se.pisd.roborally.fileaccess.SerializeState;
import dk.dtu.compute.se.pisd.roborally.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadAndSaveTheGame {
    private static final String BOARDS_FOLDER = "boards";
    private static final String SAVED_BOARDS_FOLDER = "savedBoards";
    private static final String JSON_EXT = "json";
    private static boolean newBoardCreated = false;
    final static private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");

    public static void saveBoardToDisk(Board board, String name) {
        ReadWriteGame.writeGameToDisk(name, SerializeState.serializeGame(board));
    }

    public static Board loadBoard(String name) throws TheBoardCantFoundException {
        return SerializeState.deserialiseTheGame(
                ReadWriteGame.readGameFromDisk(SAVED_BOARDS_FOLDER + "/" + name + "." + JSON_EXT),
                true
        );
    }

    public static Board newBoard(int numPlayers, String boardName) throws TheBoardCantFoundException {
        newBoardCreated = true;
        Board board = SerializeState.deserialiseTheGame(
                ReadWriteGame.readGameFromDisk(BOARDS_FOLDER + "/" + boardName + "." + JSON_EXT),
                false
        );
        AddAndCreatePlayer(board, numPlayers);
        PlayersRandomly(board.getPlayers(), getAllSpacesOfTypeByFieldAction(board, new StartGear()));
        return board;
    }

    private static void AddAndCreatePlayer(Board board, int numPlayers) {
        for (int i = 0; i < numPlayers; i++) {
            board.addPlayer(new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1)));
        }
    }

    private static void PlayersRandomly(List<Player> players, List<Space> possibleSpaces) {
        players.forEach(player -> {
            Space currentSpace = possibleSpaces.remove(0);
            player.setSpace(currentSpace);
            player.setHeading(Heading.EAST);
        });
    }

    private static List<Space> getAllSpacesOfTypeByFieldAction(Board board, FieldAction action) {
        List<Space> spaces = new ArrayList<>();
        for (int y = 0; y < board.height; y++) {
            for (int x = 0; x < board.width; x++) {
                Space curSpace = board.getSpace(x, y);
                List<FieldAction> curSpaceActions = curSpace.getActions();
                if (!curSpaceActions.isEmpty() && action.getClass().getSimpleName().equals(curSpaceActions.get(0).getClass().getSimpleName())) {
                    spaces.add(curSpace);
                }
            }
        }
        return spaces;
    }

    public static boolean getBoardCreated() {
        return newBoardCreated;
    }
}

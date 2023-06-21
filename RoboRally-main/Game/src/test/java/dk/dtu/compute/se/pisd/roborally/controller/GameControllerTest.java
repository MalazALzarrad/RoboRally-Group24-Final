package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.exceptions.TheBoardCantFoundException;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameControllerTest {

    private final int TEST_WIDTH = 8;
    private final int TEST_HEIGHT = 8;

    private GameController gameController;

    @BeforeEach
    void initialize() {
        Board board = initializeTestBoard();
        board.setCurrentPlayer(board.getPlayer(0));
    }
    @Test
    private Board initializeTestBoard() {
        Board board = new Board(TEST_WIDTH, TEST_HEIGHT);
        gameController = new GameController(null, board, null);
        for (int i = 0; i < 6; i++) {
            Player player = new Player(board, null, "Player " + i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(i, i));
            player.setHeading(Heading.values()[i % Heading.values().length]);
        }
        return board;
    }

    @AfterEach
    void cleanup() {
        gameController = null;
    }

    @Test
    void positionCurrentPlayerOnSpace() {
        Board board = gameController.board;
        Player player1 = board.getPlayer(0);
        Player player2 = board.getPlayer(1);

        gameController.moveCurrentPlayerToSpace(board.getSpace(0, 4));

        assertPlayerMoveToSpace(player1, player2, board);
    }

    private void assertPlayerMoveToSpace(Player player1, Player player2, Board board) {
        assertEquals(player1, board.getSpace(0, 4).getPlayer(), "Player " + player1.getName() + " should beSpace (0,4)!");
        assertNull(board.getSpace(0, 0).getPlayer(), "Space (0,0) should be empty!");
        assertEquals(player2, board.getCurrentPlayer(), "Current player should be " + player2.getName() + "!");
    }

    @Test
    void pushForward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.movementController.moveForward(current, 1);

        checkAndMoveForward(current, board);
    }

    private void checkAndMoveForward(Player current, Board board) {
        assertEquals(current, board.getSpace(0, 1).getPlayer(), "Player " + current.getName() + " should beSpace (0,1)!");
        assertEquals(Heading.SOUTH, current.getHeading(), "Player 0 should be heading SOUTH!");
        assertNull(board.getSpace(0, 0).getPlayer(), "Space (0,0) should be empty!");
    }

    @Test
    void checkTurnOrderOfPlayers() throws TheBoardCantFoundException {
        Board board = LoadAndSaveTheGame.newBoard(3, "Board1");
        GameController gc = initializeControllerForPlayers(board);

        List<Player> playersBefore = new ArrayList<>(board.getPlayers());
        gc.assertPlayerPriorityAndChangeBoardPlayers();
        List<Player> playersAfter = board.getPlayers();

        checkTurnOrderOfPlayers(playersBefore, playersAfter);
    }

    private GameController initializeControllerForPlayers(Board board) {
        board.setPlayers(new ArrayList<>());
        GameController gc = new GameController(null, board, null);

        // Create players
        for (int i = 0; i < 3; i++) {
            Player player = new Player(board, null, "Player " + (i+1));
            board.addPlayer(player);
            player.setSpace(board.getSpace(i + 1, 5));
            player.setHeading(Heading.values()[i % Heading.values().length]);
        }
        return gc;
    }

    private void checkTurnOrderOfPlayers(List<Player> playersBefore, List<Player> playersAfter) {
        Assertions.assertTrue(
                playersBefore.get(0) == playersAfter.get(2) &&
                        playersBefore.get(1) == playersAfter.get(0) &&
                        playersBefore.get(2) == playersAfter.get(1),
                "Player turn order does not match!"
        );
    }
}

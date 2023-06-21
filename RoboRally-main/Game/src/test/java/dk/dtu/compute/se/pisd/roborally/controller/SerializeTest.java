package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.exceptions.TheBoardCantFoundException;
import dk.dtu.compute.se.pisd.roborally.fileaccess.SerializeState;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import org.junit.jupiter.api.*;

/**
 * @author Malaz ALZarrad
 */
public class SerializeTest {

    /**
     * Tests if we are losing any information when serializing
     *
     * @author Malaz ALZarrad
     */
    @Test
    void performDefaultBoardSerializationDeserializationIteration() {
        try {
            Board start = LoadAndSaveTheGame.newBoard(2, "Board1");
            String Result1 = SerializeState.serializeGame(start);

            Board board1 = SerializeState.deserialiseTheGame(Result1, false);
            String Result2 = SerializeState.serializeGame(board1);

            Assertions.assertEquals(Result1, Result2);
        } catch (TheBoardCantFoundException e) {
            assert true;
        }
    }


}

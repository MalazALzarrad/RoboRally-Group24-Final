package dk.dtu.compute.se.pisd.roborally.exceptions;

/**
 *  @author Malaz ALZarrad
 */
public class TheBoardCantFoundException extends Exception {
    private final String boardPath;

    public TheBoardCantFoundException(String boardPath){
        this.boardPath = boardPath;
    }

}

package dk.dtu.compute.se.pisd.roborally.controller.fieldaction;
import javafx.scene.control.Alert;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

/**
 * @author Malaz ALZarrad
 */

public class Checkpoint extends FieldAction {
    private static int highestCheckpoint = 0;
    private int checkpoint;
    private int checkpointNumber;

    public int getCheckpointNumber() {
        return checkpointNumber;
    }

    public static void setHighestCheckpoint(int highestCheckpoint) {
        Checkpoint.highestCheckpoint = highestCheckpoint;
    }

    public Checkpoint() {
        highestCheckpoint++;
        checkpoint = highestCheckpoint;
    }

    @Override
    public boolean doAction(GameController gameController, Space space) {
        if (!space.getActions().isEmpty()) { // checking if the space has any actions
            Checkpoint checkpoint = (Checkpoint) space.getActions().get(0); // get the first action (assumed to be a checkpoint)
            Player player = space.getPlayer(); // get the player on the space

            // Check if the player is not null and if the player is stepping on the checkpoint in the correct order
            if (player != null && (player.checkPoints + 1 == checkpoint.checkpoint)) {
                player.checkPoints++; // increment the player's checkpoints

                // Check if the player has won the game
                if (player.checkPoints == highestCheckpoint) {
                    displayWinnerMessage(space); // Show win message
                    highestCheckpoint = 0; // Reset the static variable for the highest checkpoint number
                    gameController.board.gameOver = true; // Mark the game as over
                    gameController.pushGameState(); // Push the game state
                    gameController.endGame(); // End the game
                }
                return true;
            }
        }
        return false; // Return false if no action was taken
    }

    private void displayWinnerMessage(Space space) {
        Alert winningAlert = new Alert(Alert.AlertType.INFORMATION);
        Player winner = space.getPlayer();

        winningAlert.setTitle("End of Game");
        winningAlert.setHeaderText(String.format("The winner is: %s", winner.getName()));
        winningAlert.setContentText("Game has ended, the application will now exit.");

        winningAlert.showAndWait();
    }

}

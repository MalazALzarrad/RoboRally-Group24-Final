package dk.dtu.compute.se.pisd.roborally.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

import java.util.List;
import java.util.Optional;

/**
 * This class handle all the different styles of popup boxes.
 *
 * @author Malaz ALZarrad
 */
public class AlerBoxes {

    public Optional<ButtonType> alertBox(String title, String contentText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(contentText);
        return alert.showAndWait();
    }

    public String getInputString(String title, String header) {
        TextInputDialog serverCreation = new TextInputDialog();
        serverCreation.setTitle(title);
        serverCreation.setHeaderText(header);
        Optional<String> decision = serverCreation.showAndWait();
        return decision.orElse(null);
    }

    public Optional<Integer> promptForPlayersNumber(String title, String header, List<Integer> list) {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        return dialog.showAndWait();
    }

    public Optional<String> promptForBoardName(String title, String header, List<String> list) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        return dialog.showAndWait();
    }
}

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
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

/**
 * Show each player tab and all the buttons
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class PlayerView extends Tab implements ViewObserver {

    private final Player player;
    private final GridPane programPane;
    private CardFieldView[] programCardViews;
    private final VBox buttonPanel;
    private final VBox playerInteractionPanel;
    private final GameController gameController;

    private Button finishButton;
    private Button executeButton;
    private Button stepButton;

    public PlayerView(@NotNull GameController gameController, @NotNull Player player) {
        super(player.getName());
        this.setStyle("-fx-text-base-color: " + player.getColor() + ";");
        this.gameController = gameController;
        this.player = player;

        VBox top = new VBox();
        this.setContent(top);

        programPane = createProgramPane();
        buttonPanel = createButtonPanel();
        playerInteractionPanel = createPlayerInteractionPanel();
        GridPane cardsPane = createCardsPane();

        top.getChildren().addAll(
                new Label("Program"),
                programPane,
                new Label("Command Cards"),
                cardsPane
        );

        if (player.board != null) {
            player.board.attach(this);
            updateView(player.board);
        }
    }

    private GridPane createProgramPane() {
        GridPane programPane = new GridPane();
        programPane.setVgap(2.0);
        programPane.setHgap(2.0);
        programCardViews = new CardFieldView[Player.NO_REGISTERS];
        for (int i = 0; i < Player.NO_REGISTERS; i++) {
            CommandCardField cardField = player.getProgramField(i);
            if (cardField != null) {
                programCardViews[i] = new CardFieldView(gameController, cardField);
                programPane.add(programCardViews[i], i, 0);
            }
        }
        return programPane;
    }

    private VBox createButtonPanel() {
        finishButton = new Button("Finish Programming");
        finishButton.setOnAction(e -> gameController.finishProgrammingPhase());

        executeButton = new Button("Execute Program");
        executeButton.setOnAction(e -> gameController.executePrograms());

        stepButton = new Button("Execute Current Register");
        stepButton.setOnAction(e -> gameController.executeStep());

        VBox buttonPanel = new VBox(finishButton, executeButton, stepButton);
        buttonPanel.setAlignment(Pos.CENTER_LEFT);
        buttonPanel.setSpacing(3.0);

        return buttonPanel;
    }

    private VBox createPlayerInteractionPanel() {
        VBox playerInteractionPanel = new VBox();
        playerInteractionPanel.setAlignment(Pos.CENTER_LEFT);
        playerInteractionPanel.setSpacing(3.0);
        return playerInteractionPanel;
    }

    private GridPane createCardsPane() {
        GridPane cardsPane = new GridPane();
        cardsPane.setVgap(2.0);
        cardsPane.setHgap(2.0);
        for (int i = 0; i < Player.NO_CARDS; i++) {
            CommandCardField cardField = player.getCardField(i);
            if (cardField != null) {
                CardFieldView cardFieldView = new CardFieldView(gameController, cardField);
                cardsPane.add(cardFieldView, i, 0);
            }
        }
        return cardsPane;
    }


    @Override
    public void updateView(Subject subject) {
        if (subject == player.board) {
            updateCardBackgrounds();

            if (player.board.getPhase() != Phase.PLAYER_INTERACTION) {
                updateProgramPaneWithButtonPanel();
                updateButtonsAvailability();
            } else {
                updateProgramPaneWithPlayerInteractionPanel();
            }
        }
    }

    private void updateCardBackgrounds() {
        for (int i = 0; i < Player.NO_REGISTERS; i++) {
            CardFieldView cardFieldView = programCardViews[i];
            if (cardFieldView != null) {
                cardFieldView.updateBackground(player, i);
            }
        }
    }

    private void updateProgramPaneWithButtonPanel() {
        if (!programPane.getChildren().contains(buttonPanel)) {
            programPane.getChildren().remove(playerInteractionPanel);
            programPane.add(buttonPanel, Player.NO_REGISTERS, 0);
        }
    }

    private void updateButtonsAvailability() {
        switch (player.board.getPhase()) {
            case INITIALISATION -> setButtonsDisabled(true, false, true);
            case PROGRAMMING -> setButtonsDisabled(gameController.isMyTurn(), true, true);
            case ACTIVATION -> setButtonsDisabled(true, gameController.isMyTurn(), gameController.isMyTurn());
            default -> setButtonsDisabled(true, true, true);
        }
    }

    private void setButtonsDisabled(boolean finishButtonStatus, boolean executeButtonStatus, boolean stepButtonStatus) {
        finishButton.setDisable(finishButtonStatus);
        executeButton.setDisable(executeButtonStatus);
        stepButton.setDisable(stepButtonStatus);
    }

    private void updateProgramPaneWithPlayerInteractionPanel() {
        if (!programPane.getChildren().contains(playerInteractionPanel)) {
            programPane.getChildren().remove(buttonPanel);
            programPane.add(playerInteractionPanel, Player.NO_REGISTERS, 0);
        }
        updatePlayerInteractionPanel();
    }

    private void updatePlayerInteractionPanel() {
        playerInteractionPanel.getChildren().clear();
        if (player.board.getCurrentPlayer() == player) {
            Command current = player.getProgramField(player.board.getStep()).getCard().command;
            if (current.isInteractive()) {
                current.getOptions().forEach(this::addOptionButtonToInteractionPanel);
            }
        }
    }

    private void addOptionButtonToInteractionPanel(Command command) {
        Button optionButton = new Button(command.toString());
        optionButton.setOnAction(e -> gameController.executeCommandAndResumeActivation(command));
        optionButton.setDisable(false);
        playerInteractionPanel.getChildren().add(optionButton);
    }
}

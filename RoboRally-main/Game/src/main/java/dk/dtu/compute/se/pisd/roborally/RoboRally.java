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
package dk.dtu.compute.se.pisd.roborally;

import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import dk.dtu.compute.se.pisd.roborally.view.AlerBoxes;
import dk.dtu.compute.se.pisd.roborally.view.RoboRallyMenuBar;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RoboRally extends Application {
    private static final int MIN_APP_WIDTH = 600;
    private Stage stage;
    private BorderPane boardRoot;
    private BoardView boardView;
    private AlerBoxes popupBoxes;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        AppController appController = new AppController(this);
        initializeUI(appController);

        stage.setOnCloseRequest(
                e -> {
                    e.consume();
                    appController.exit();
                });
        stage.setResizable(true);
        stage.sizeToScene();
        stage.show();
    }

    private void initializeUI(AppController appController) {
        RoboRallyMenuBar menuBar = new RoboRallyMenuBar(appController);
        this.boardRoot = new BorderPane();
        VBox vbox = new VBox(menuBar, boardRoot);
        vbox.setMinWidth(MIN_APP_WIDTH);
        Scene primaryScene = new Scene(vbox);
        stage.setScene(primaryScene);
        stage.setTitle("RoboRally");
    }

    public void createBoardView(GameController gameController) {
        clearBoardRoot();
        // create and add view for new board

        if (gameController != null) {
            boardView = new BoardView(gameController);
            boardRoot.setCenter(boardView);
        }

        stage.sizeToScene();
    }

    private void clearBoardRoot() {
        if (boardRoot != null) {
            boardRoot.getChildren().clear();
        }
    }

    public BoardView getBoardView() {
        return boardView;
    }

    public AlerBoxes getPopupBoxes() {
        if (popupBoxes == null) {
            popupBoxes = new AlerBoxes();
        }
        return popupBoxes;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

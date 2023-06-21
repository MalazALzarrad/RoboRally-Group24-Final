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
import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.event.ActionEvent;

public class RoboRallyMenuBar extends MenuBar {

    private final AppController appController;

    private final MenuItem newGame;
    private final MenuItem endGame;
    private final MenuItem saveGame;
    private final MenuItem loadGame;

    private final MenuItem launchServer;
    private final MenuItem connectToGameServer;
    private final MenuItem disconnectGameServer;

    public RoboRallyMenuBar(AppController appController) {
        this.appController = appController;

        Menu controlMenu = createMenu("File");
        Menu serverMenu = createMenu("Joint-play");
//A lambda expression is a short block of code which takes in parameters and returns a value.
// Lambda expressions are similar to methods,
// but they do not need a name and they can be implemented right in the body of a method.
        newGame = createMenuItem("New Game", e -> appController.newGame());
        endGame = createMenuItem("Stop Game", e -> appController.stopGame());
        saveGame = createMenuItem("Save Game", e -> appController.saveGame());
        loadGame = createMenuItem("Load Game", e -> appController.loadGame());

        MenuItem exitApp = createMenuItem("Exit", e -> appController.exit());

        addToMenu(controlMenu, newGame, endGame, saveGame, loadGame, exitApp);

        launchServer = createMenuItem("Launch game", e -> {
            appController.stopGame();
            appController.hostGame();
        });
        connectToGameServer = createMenuItem("Connect with server", e -> appController.connectToServer());
        disconnectGameServer = createMenuItem("Shut down server connection", e -> {
            appController.disconnectFromServer();
            appController.stopGame();
        });

        addToMenu(serverMenu, launchServer, connectToGameServer, disconnectGameServer);

        setOnShowing(controlMenu, serverMenu);
        update();
    }

    private Menu createMenu(String name) {
        Menu menu = new Menu(name);
        this.getMenus().add(menu);
        return menu;
    }

    private MenuItem createMenuItem(String name, javafx.event.EventHandler<ActionEvent> event) {
        MenuItem menuItem = new MenuItem(name);
        menuItem.setOnAction(event);
        return menuItem;
    }

    private void addToMenu(Menu menu, MenuItem... items) {
        menu.getItems().addAll(items);
    }

    private void setOnShowing(Menu... menus) {
        for (Menu menu : menus) {
            menu.setOnShowing(e -> update());
            menu.setOnShown(e -> updateBounds());
        }
    }

    public void update() {
        boolean gameRunning = appController.isGameRunning();
        newGame.setVisible(!gameRunning);
        endGame.setVisible(gameRunning);
        saveGame.setVisible(gameRunning);
        loadGame.setVisible(!gameRunning);

        launchServer.setVisible(true);
        connectToGameServer.setVisible(true);
        disconnectGameServer.setVisible(true);
    }
}

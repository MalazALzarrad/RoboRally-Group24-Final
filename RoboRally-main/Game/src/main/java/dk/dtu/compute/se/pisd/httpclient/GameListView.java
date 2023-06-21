package dk.dtu.compute.se.pisd.httpclient;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import java.io.StringReader;


public class GameListView {
    private Stage stage;
    private TableView<Game> table = new TableView<>();
    private ObservableList<Game> data = FXCollections.observableArrayList();
    private AppController app;

    public GameListView(Client c, AppController app) {
        this.app = app;
        stage = new Stage();
        Scene scene = new Scene(new Group());

        stage.setTitle("List of Games");
        stage.setWidth(650);
        stage.setHeight(580);

        Label label = createLabel();
        createTableColumns();
        Button joinButton = createJoinButton();
        Button refreshButton = createRefreshButton(c);
        VBox vbox = createVBox(label, joinButton, refreshButton);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);
        stage.setScene(scene);
    }

    private Label createLabel() {
        final Label label = new Label("Available games\nselect one");
        label.setFont(new Font("Arial", 20));
        return label;
    }

    private void createTableColumns() {
        table.setEditable(false);

        TableColumn id = createTableColumn("ID", 50, Game::getId);
        TableColumn serverName = createTableColumn("Server Name", 450, Game::getTheTitle);
        TableColumn players = createTableColumn("Players", 50, Game::getPlayersAmount);
        TableColumn maxPlayers = createTableColumn("Max Players", 50, Game::getMaxPlayersAmount);
        maxPlayers.setResizable(false);

        table.setItems(data);
        table.getColumns().addAll(id, serverName, players, maxPlayers);
    }

    private <T> TableColumn<Game, T> createTableColumn(String text, int width, Function<Game, T> propertyGetter) {
        TableColumn<Game, T> column = new TableColumn<>(text);
        column.setPrefWidth(width);
        column.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(propertyGetter.apply(p.getValue())));
        return column;
    }

    private Button createJoinButton() {
        Button button = new Button("Join Game");
        button.setOnAction(e -> {
            app.stopGame();
            if (!table.getSelectionModel().isEmpty()) app.joinGame(table.getSelectionModel().getSelectedItem().getId());
            stage.close();
        });
        return button;
    }

    private Button createRefreshButton(Client c) {
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> addTheServer(c.listOfTheGames()));
        return refresh;
    }

    private VBox createVBox(Label label, Button joinButton, Button refreshButton) {
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table, joinButton, refreshButton);
        return vbox;
    }


    public void addTheServer(String s) {
        Gson gson = new Gson();
        try (JsonReader jReader = new JsonReader(new StringReader(s))) {
            Game[] games = gson.fromJson(jReader, Game[].class);
            data.clear();
            data.addAll(Arrays.asList(games));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void viewTheTable() {
        stage.show();
    }
}

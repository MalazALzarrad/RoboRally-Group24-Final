package dk.dtu.compute.se.pisd.roborally.fileaccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.*;
import dk.dtu.compute.se.pisd.roborally.model.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SerializeState {
/*The serializeGame method takes a Board object as an input and returns a JSON string representing the state of
the game. It uses helper methods to create BoardTemplate and PlayerTemplate objects,
 which are then converted to JSON using the Gson library.
 */
    public static String serializeGame(Board board) {
        BoardTemplate template = setupBoardTemplate(board);
        List<Player> players = board.getPlayers();
        List<PlayerTemplate> playersTemplate = createPlayersTemplate(players);
        template.players = playersTemplate;

        if (board.getCurrentPlayer() == null)
            template.currentPlayer = 0;
        else
            template.currentPlayer = board.getPlayerNumber(board.getCurrentPlayer());

        return saveBoardTemplateUsingGson(template);
    }

    private static BoardTemplate setupBoardTemplate(Board board) {
        BoardTemplate template = new BoardTemplate();
        template.width = board.width;
        template.height = board.height;
        template.phase = board.phase.toString();
        template.step = board.step;
        template.stepMode = board.stepMode;
        template.theGameIsOver = board.gameOver;

        for (int i = 0; i < board.width; i++) {
            for (int j = 0; j < board.height; j++) {
                Space space = board.getSpace(i, j);
                if (!space.getWalls().isEmpty() || !space.getActions().isEmpty()) {
                    SpaceTemplate spaceTemplate = new SpaceTemplate();
                    spaceTemplate.x = space.x;
                    spaceTemplate.y = space.y;
                    spaceTemplate.actions.addAll(space.getActions());
                    spaceTemplate.walls.addAll(space.getWalls());
                    template.spaces.add(spaceTemplate);
                }
            }
        }
        return template;
    }

    private static List<PlayerTemplate> createPlayersTemplate(List<Player> players) {
        List<PlayerTemplate> playersTemplate = new ArrayList<>();

        for (Player player : players) {
            PlayerTemplate playerTemplate = new PlayerTemplate();
            CommandCardFieldTemplate[] programTemplate = new CommandCardFieldTemplate[player.program.length];
            CommandCardFieldTemplate[] cardsTemplate = new CommandCardFieldTemplate[player.cards.length];
            playerTemplate.name = player.name;
            playerTemplate.color = player.color;
            playerTemplate.checkPoints = player.checkPoints;
            playerTemplate.priority = player.priority;
            playerTemplate.spaceX = player.space.x;
            playerTemplate.spaceY = player.space.y;
            playerTemplate.heading = player.heading.toString();
            cardsTemplate = createCardsTemplate(player, cardsTemplate);
            programTemplate = createProgramTemplate(player, programTemplate);
            playerTemplate.program = programTemplate;
            playerTemplate.cards = cardsTemplate;

            playersTemplate.add(playerTemplate);
        }
        return playersTemplate;
    }

    private static CommandCardFieldTemplate[] createCardsTemplate(Player player, CommandCardFieldTemplate[] cardsTemplate) {
        return createCommandCardFieldTemplate(player.cards, cardsTemplate);
    }

    private static CommandCardFieldTemplate[] createProgramTemplate(Player player, CommandCardFieldTemplate[] programTemplate) {
        return createCommandCardFieldTemplate(player.program, programTemplate);
    }

    private static CommandCardFieldTemplate[] createCommandCardFieldTemplate(CommandCardField[] cards, CommandCardFieldTemplate[] commandCardFieldTemplates) {
        for (int j = 0; j < cards.length; j++) {
            CommandCardField card = cards[j];
            CommandCardFieldTemplate commandCardFieldTemplate = new CommandCardFieldTemplate();
            CommandCardTemplate commandCardTemplate = new CommandCardTemplate();
            CommandTemplate commandTemplate = new CommandTemplate();

            if (card.card == null) {
                commandTemplate.type = "";
            } else {
                commandTemplate.type = card.card.command.name();
                List<String> options = new ArrayList<>();
                for (Command option : card.card.command.options) {
                    options.add(String.valueOf(option));
                }
            }
            commandCardTemplate.command = commandTemplate;
            commandCardFieldTemplate.card = commandCardTemplate;
            commandCardFieldTemplate.visible = card.visible;

            commandCardFieldTemplates[j] = commandCardFieldTemplate;
        }
        return commandCardFieldTemplates;
    }

    private static String saveBoardTemplateUsingGson(BoardTemplate template) {
        GsonBuilder simpleBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                setPrettyPrinting();
        Gson gson = simpleBuilder.create();

        return gson.toJson(template, template.getClass());
    }

/*The deserialiseTheGame method does the opposite, it takes a JSON string and returns a Board object.
It first converts the JSON string to a BoardTemplate object using Gson. Then, it uses helper methods to create a new Board object and fill it with the data from the BoardTemplate.
 */


    public static Board deserialiseTheGame(String jsonString, boolean savedGame) {
        BoardTemplate template = getBoardTempFromJsonStr(jsonString);
        Board result = setupTheBoard(template, savedGame);
        result.setCheckpointsWithNumber();
        loadingPlayersIntoBoard(template, result, savedGame);
        return result;
    }

    private static BoardTemplate getBoardTempFromJsonStr(String jsonString) {
        GsonBuilder simpleBuilder = new GsonBuilder().registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>());
        Gson gson = simpleBuilder.create();

        JsonReader reader = gson.newJsonReader(new StringReader(jsonString));
        return gson.fromJson(reader, BoardTemplate.class);
    }

    private static Board setupTheBoard(BoardTemplate template, boolean savedGame) {
        Board result = new Board(template.width, template.height);
        if (savedGame) {
            result.phase = Phase.valueOf(template.phase);
            result.step = template.step;
            result.stepMode = template.stepMode;
            result.gameOver = template.theGameIsOver;
        }

        for (SpaceTemplate spaceTemplate : template.spaces) {
            Space space = result.getSpace(spaceTemplate.x, spaceTemplate.y);
            if (space != null) {
                space.getActions().addAll(spaceTemplate.actions);
                space.getWalls().addAll(spaceTemplate.walls);
                space.setPlayer(null);
            }
        }
        return result;
    }

    private static void loadingPlayersIntoBoard(BoardTemplate template, Board result, boolean savedGame) {
        for (int i = 0; i < template.players.size(); i++) {
            PlayerTemplate playerTemplate = template.players.get(i);
            Player newPlayer = new Player(result, playerTemplate.color, playerTemplate.name);
            result.addPlayer(newPlayer);

            newPlayer.setSpace(result.getSpace(playerTemplate.spaceX, playerTemplate.spaceY));
            newPlayer.heading = Heading.valueOf(playerTemplate.heading);
            newPlayer.checkPoints = playerTemplate.checkPoints;
            newPlayer.priority = playerTemplate.priority;

            CommandCardField[] newCards = loadingCards(playerTemplate.cards, newPlayer);
            CommandCardField[] newProgram = loadingProgram(playerTemplate.program, newPlayer);

            newPlayer.cards = newCards;
            newPlayer.program = newProgram;
        }

        if (savedGame) {
            int currentPlayerIndex = template.currentPlayer;
            result.setCurrentPlayer(result.getPlayer(currentPlayerIndex));
        }
    }

    private static CommandCardField[] loadingCards(CommandCardFieldTemplate[] cardsTemplate, Player newPlayer) {
        return loadingCommandCardField(cardsTemplate, newPlayer);
    }

    private static CommandCardField[] loadingProgram(CommandCardFieldTemplate[] programTemplate, Player newPlayer) {
        return loadingCommandCardField(programTemplate, newPlayer);
    }

    private static CommandCardField[] loadingCommandCardField(CommandCardFieldTemplate[] commandCardFieldTemplates, Player newPlayer) {
        CommandCardField[] newCommandCardField = new CommandCardField[commandCardFieldTemplates.length];
        for (int j = 0; j < commandCardFieldTemplates.length; j++) {
            String commandName = commandCardFieldTemplates[j].card.command.type;
            CommandCardField ccf = new CommandCardField(newPlayer);
            if (!commandName.equals("")) {
                Command c = Command.valueOf(commandName);
                CommandCard cc = new CommandCard(c);
                ccf.setCard(cc);
                ccf.setVisible(commandCardFieldTemplates[j].visible);
            }
            newCommandCardField[j] = ccf;
        }
        return newCommandCardField;
    }

}

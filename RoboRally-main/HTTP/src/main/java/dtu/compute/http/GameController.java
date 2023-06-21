package dtu.compute.http;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;


@Service
public class GameController implements IRoboRallyServerClient {
    ArrayList<Game> games = new ArrayList<>();
    private int id = 0;

    @Override
    public void updateTheGame(String id, String gameState) {
        Game game = findTheServer(id);
        if (game != null) {
            game.setGameState(gameState);
            if (game.getMaxAmountOfPlayers() == 0) //if the max amount of player is set, we are done
                game.setMaxAmountOfPlayers(StringUtils.countOccurrencesOf(gameState, "Player "));
        }
    }


    @Override
    public String getStateOfTheGame(String serverId) {
        Game game = findTheServer(serverId);
        return game != null ? game.getGameState() : null;
    }


    @Override
    public String hostOfTheGame(String title) {
        Game game = new Game(title, id);
        games.add(game);
        id++;
        return game.getId();
    }


    @Override
    public String listOfTheGames() {
        Gson gson = new Gson();

        ArrayList<Game> game = new ArrayList<>();
        games.stream()
                .filter(e -> e.getAmountOfPlayers() != e.getMaxAmountOfPlayers())
                .forEach(game::add);

        return gson.toJson(game);
    }


    @Override
    public String joinTheGame(String serverToJoin) {
        Game s = findTheServer(serverToJoin);
        if (s == null)
            return "Server doesn't exist";
        if (s.getAmountOfPlayers() >= s.getMaxAmountOfPlayers())
            return "Server is full";
        s.addPlayer();
        return String.valueOf(s.getARobot());
    }


    @Override
    public void leaveTheGame(String serverId, int robot) {
        Game game = findTheServer(serverId);
        if (game == null)
            return;
        if (game.getMaxAmountOfPlayers() != 0)
            game.setPlayerSpotFilled(robot, false);
        game.removePlayer();
        if (game.isEmpty())
            games.remove(game);
    }


    private Game findTheServer(String serverId) {
        return games.stream()
                .filter(s -> s.getId().equals(serverId))
                .findFirst()
                .orElse(null);
    }
}

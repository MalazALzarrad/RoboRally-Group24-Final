package dtu.compute.http;

/**
 * Interface between client and server
 *
 * @author Malaz ALZarrad
 */
public interface IRoboRallyServerClient {
    void updateTheGame(String id, String gameState);

    String getStateOfTheGame(String serverId);

    String hostOfTheGame(String title);

    String listOfTheGames();

    String joinTheGame(String serverToJoin);

    void leaveTheGame(String serverId, int i);
}


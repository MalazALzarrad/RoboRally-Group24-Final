package dk.dtu.compute.se.pisd.httpclient;


public interface IRoboRallyServerClient {
    void updateTheGame(String gameState);

    String getStateOfTheGame();

    String hostOfTheGame(String title);

    String listOfTheGames();

    String joinTheGame(String serverToJoin);

    void leaveTheGame();
}

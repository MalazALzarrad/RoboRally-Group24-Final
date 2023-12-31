package dtu.compute.http;

public class Game {
    private String id;
    private String title; //is used in the Gson Json converter
    private transient String gameState;
    private int amountOfPlayers;
    private int maxAmountOfPlayers;
    private transient boolean[] playerSpotFilled;

    public Game(String title, int id) {
        this.id = String.valueOf(id);
        this.title = title;
        this.amountOfPlayers = 1;
    }

    public void addPlayer() {
        amountOfPlayers++;
    }

    public void removePlayer() {
        amountOfPlayers--;
    }

    public boolean isEmpty() {
        return amountOfPlayers == 0;
    }

    public int getAmountOfPlayers() {
        return amountOfPlayers;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public String getId() {
        return id;
    }

    public int getMaxAmountOfPlayers() {
        return maxAmountOfPlayers;
    }

    public void setMaxAmountOfPlayers(int amountOfPlayers) {
        this.maxAmountOfPlayers = amountOfPlayers;
        this.playerSpotFilled = new boolean[amountOfPlayers];
        playerSpotFilled[0] = true;
    }

    public int getARobot() {
        if (playerSpotFilled == null) return 0;
        for (int i = 0; i < maxAmountOfPlayers; i++)
            if (!playerSpotFilled[i]) {
                setPlayerSpotFilled(i, true);
                return i;
            }
        return 0;
    }

    public void setPlayerSpotFilled(int i, boolean flag) {
        if (playerSpotFilled != null && i >= 0 && i < playerSpotFilled.length) {
            playerSpotFilled[i] = flag;
        }
    }
}

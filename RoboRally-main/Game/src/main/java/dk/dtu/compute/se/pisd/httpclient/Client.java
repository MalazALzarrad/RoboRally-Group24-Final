package dk.dtu.compute.se.pisd.httpclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import static java.util.concurrent.TimeUnit.*;

public class Client implements IRoboRallyServerClient {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10)).build();
    private String TheServer = "http://localhost:8080";    // The URL of the game server. Can be updated later to retrieve this information from a DNS request or directly point to a server IP.
    private String serverID = "";                       // The unique identifier for the game we are currently in. This will be used after establishing a connection to inform the server about the specific game.
    private boolean connectedToServer = false;          // Indicates whether we are currently connected to a server. Helps in checking if we need to disconnect from the current server before connecting to a new one.
    private int robotNumber;                            // The assigned number for our robot in the game. Used primarily to release our robot's slot if we decide to leave an ongoing game before its completion.


    public boolean hasServerConnection() {
        return connectedToServer;
    }


    @Override
    public void updateTheGame(String gameState) {
        HttpRequest request = createRequest(gameState);

        CompletableFuture<HttpResponse<String>> response =
                HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        handleResponse(response);
    }

    private HttpRequest createRequest(String gameState) {
        return HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(gameState))
                .uri(URI.create(TheServer + "/gameState/" + serverID))
                .setHeader("User-Agent", "RoboRally Client")
                .setHeader("Content-Type", "application/json")
                .build();
    }

    private void handleResponse(CompletableFuture<HttpResponse<String>> response) {
        try {
            String result = response.thenApply(HttpResponse::body).get(5, SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public String getStateOfTheGame() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(TheServer + "/gameState/" + serverID))
                .setHeader("User-Agent", "RoboRally Client")
                .header("Content-Type", "application/json")
                .build();
        CompletableFuture<HttpResponse<String>> response =
                HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        String result;
        try {
            result = response.thenApply(HttpResponse::body).get(5, SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        return result;
    }



    @Override
    public String hostOfTheGame(String title) {
        if (!serverID.isEmpty())
            leaveTheGame();

        HttpRequest request = formPostRequest(title);

        CompletableFuture<HttpResponse<String>> response =
                HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return processHostingResponse(response);
    }

    private HttpRequest formPostRequest(String title) {
        return HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(title))
                .uri(URI.create(TheServer + "/game"))
                .setHeader("User-Agent", "RoboRally Client")
                .header("Content-Type", "text/plain")
                .build();
    }

    private String processHostingResponse(CompletableFuture<HttpResponse<String>> response) {
        try {
            serverID = response.thenApply(HttpResponse::body).get(5, SECONDS);

            HttpResponse<String> httpResponse = response.get();
            if (httpResponse.statusCode() == 500) {
                return httpResponse.body();
            }

            connectedToServer = true;
            robotNumber = 0;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            serverID = "";
            return "Service timeout";
        }

        return "success";
    }


    @Override
    public String listOfTheGames() {
        HttpRequest request = createGetRequest();
        CompletableFuture<HttpResponse<String>> response =
                HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return handleListOfGamesReply(response);
    }

    private HttpRequest createGetRequest() {
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(TheServer + "/game"))
                .setHeader("User-Agent", "RoboRally Client")
                .header("Content-Type", "application/json")
                .build();
    }

    private String handleListOfGamesReply(CompletableFuture<HttpResponse<String>> response) {
        try {
            return response.thenApply(HttpResponse::body).get(5, SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return "server timeout";
        }
    }


    @Override
    public String joinTheGame(String serverToJoin) {
        if (!Objects.equals(serverToJoin, "")) {
            leaveTheGame();
        }

        HttpRequest request = createPutRequest(serverToJoin);
        CompletableFuture<HttpResponse<String>> response =
                HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return handleGameJoinResponse(response, serverToJoin);
    }

    private HttpRequest createPutRequest(String serverToJoin) {
        return HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .uri(URI.create(TheServer + "/game/" + serverToJoin))
                .header("User-Agent", "RoboRally Client")
                .header("Content-Type", "text/plain")
                .build();
    }

    private String handleGameJoinResponse(CompletableFuture<HttpResponse<String>> response, String serverToJoin) {
        try {
            HttpResponse<String> message = response.get(5, SECONDS);
            if (message.statusCode() == 404) {
                return message.body();
            }

            robotNumber = Integer.parseInt(message.body());
            serverID = serverToJoin;

            return "ok";
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return "service timeout";
        }
    }


    @Override
    public void leaveTheGame() {
        if (Objects.equals(serverID, "")) {
            return;
        }

        HttpRequest request = createPostRequestForLeave();
        sendRequestInNewThread(request);
        serverID = "";
    }

    private HttpRequest createPostRequestForLeave() {
        return HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .uri(URI.create(TheServer + "/game/" + serverID + "/" + robotNumber))
                .header("User-Agent", "RoboRally Client")
                .header("Content-Type", "text/plain")
                .build();
    }

    private void sendRequestInNewThread(HttpRequest request) {
        new Thread(() -> {
            int retries = 0;
            while (retries < 10) {
                CompletableFuture<HttpResponse<String>> response =
                        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                try {
                    response.get(5, SECONDS);
                    return;
                } catch (InterruptedException | ExecutionException e) {
                    return;
                } catch (TimeoutException e) {
                    retries++;
                }
            }
        }).start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public int getNumberOfRobot() {
        return robotNumber;
    }
}

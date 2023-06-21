package dtu.compute.http;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ServerController {

    private final IRoboRallyServerClient statusComm;

    public ServerController(IRoboRallyServerClient statusComm) {
        this.statusComm = statusComm;
    }

    @PostMapping(value = "/game")
    public ResponseEntity<String> createGame(@RequestBody String s) {
        String newServerID = statusComm.hostOfTheGame(s);
        if (newServerID == null)
            return ResponseEntity.internalServerError().body("Server couldn't start");
        return ResponseEntity.ok().body(newServerID);
    }

    @GetMapping(value = "/game")
    public ResponseEntity<String> listOfGames() {
        return ResponseEntity.ok().body(statusComm.listOfTheGames());
    }

    @PutMapping(value = "/game/{id}")
    public ResponseEntity<String> joinGame(@PathVariable String id) {
        String response = statusComm.joinTheGame(id);
        if (response.equals("Server doesn't exist"))
            return ResponseEntity.status(404).body(response);
        if (response.equals("Server is full"))
            return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping(value = "/game/{id}/{robot}")
    public void leaveGame(@PathVariable String id, @PathVariable String robot) {
        statusComm.leaveTheGame(id, Integer.parseInt(robot));
    }

    @GetMapping(value = "/gameState/{id}")
    public ResponseEntity<String> getGameState(@PathVariable String id) {
        return ResponseEntity.ok().body(statusComm.getStateOfTheGame(id));
    }

    @PutMapping(value = "/gameState/{id}")
    public ResponseEntity<String> setGameState(@PathVariable String id, @RequestBody String game) {
        statusComm.updateTheGame(id, game);
        return ResponseEntity.ok().body("ok");
    }
}

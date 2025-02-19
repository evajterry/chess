
package server;

import handlers.DeleteAllData;
import model.AuthData;
import org.eclipse.jetty.server.Authentication;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.*;
import chess.*;
import com.google.gson.Gson;

public class Server {
    private AuthService authService;
    private GameService gameService;
    private UserService userService;
    private final DeleteAllData deleteAllDataHandler;

    public Server() {
        this.authService = authService;
        this.gameService = gameService;
        this.userService = userService;
        this.deleteAllDataHandler = new DeleteAllData(authService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");
        Spark.get("/db", (req, res) -> "Database route active!");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", deleteAllDataHandler::handle);

        // This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();
        System.out.println(Spark.routes());

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

}


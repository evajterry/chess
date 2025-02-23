
package server;

import dataaccess.AuthAccess;
import dataaccess.GameAccess;
import dataaccess.UserAccess;
import handlers.DeleteAllData;
import handlers.RegisterUser;
import model.AuthData;
import org.eclipse.jetty.server.Authentication;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.*;
import chess.*;
import com.google.gson.Gson;

public class Server {
    private final AuthService authService;
    private final GameService gameService;
    private final UserService userService;
    private final DeleteAllData deleteAllDataHandler;
    private final RegisterUser registerUserHandler; // update

    public Server() {

        this.authService = new AuthService(new AuthAccess());  // Assuming AuthAccess has a default constructor
        this.gameService = new GameService(new GameAccess());
        this.userService = new UserService(new UserAccess());
        this.deleteAllDataHandler = new DeleteAllData(authService, userService, gameService);
        this.registerUserHandler = new RegisterUser(userService); // update
        this.loginUserHandler = new LoginUser(userService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");
        Spark.get("/db", (req, res) -> "Database route active!");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", deleteAllDataHandler::handle);
        Spark.post("/user", registerUserHandler::handle);
        Spark.post("/session", loginUserHandler::handle);
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


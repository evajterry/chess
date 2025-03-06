
package server;

import dataaccess.AuthAccess;
import dataaccess.GameAccess;
import dataaccess.UserAccess;
import handlers.*;
import handlers.exception.ResponseException;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {
    private final AuthService authService;
    private final GameService gameService;
    private final UserService userService;
    private final DeleteAllData deleteAllDataHandler;
    private final RegisterUser registerUserHandler; // update
    private final LoginUser loginUserHandler;
    private final LogoutUser logoutUserHandler;
    private final CreateNewGame createNewGameHandler;
    private final JoinGame joinGameHandler;
    private final ListGames listGamesHandler;

    public Server() {
        UserAccess userAccess = new UserAccess();  // Create UserAccess instance
        GameAccess gameAccess = new GameAccess(userAccess);

        this.authService = new AuthService(new AuthAccess()); // sql !!!!!
        this.gameService = new GameService(gameAccess, userAccess);  // Pass gameAccess to GameService
        this.userService = new UserService(userAccess);

        this.deleteAllDataHandler = new DeleteAllData(authService, userService, gameService);
        this.registerUserHandler = new RegisterUser(userService, authService); // update
        this.loginUserHandler = new LoginUser(userService, authService);
        this.logoutUserHandler = new LogoutUser(userService);
        this.createNewGameHandler = new CreateNewGame(gameService, authService);
        this.joinGameHandler = new JoinGame(gameService);
        this.listGamesHandler = new ListGames(gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");
        Spark.get("/db", (req, res) -> "Database route active!");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", deleteAllDataHandler::handle);
        Spark.post("/user", registerUserHandler::handle);
        Spark.post("/session", loginUserHandler::handle);
        Spark.delete("/session", logoutUserHandler::handle);
        Spark.post("/game", createNewGameHandler::handle);
        Spark.put("/game", joinGameHandler::handle);
        Spark.get("/game", listGamesHandler::handle);

        Spark.exception(ResponseException.class, this::exceptionHandler);
        Spark.exception(Exception.class, this::exceptionHandler);
        // This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();
        System.out.println(Spark.routes());

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.statusCode());
        res.body(ex.toJson());
    }

    private void exceptionHandler(Exception ex, Request req, Response res) {
        res.status(500);
        res.body(ex.getMessage());
        ex.printStackTrace();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

}


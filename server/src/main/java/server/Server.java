package server;

import dataaccess.*;
import handlers.*;
import exception.ResponseException;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {
    private final AuthService authService;
    private final GameService gameService;
    private final UserService userService;
    private final DeleteAllData deleteAllDataHandler;
    private final RegisterUser registerUserHandler;
    private final LoginUser loginUserHandler;
    private final LogoutUser logoutUserHandler;
    private final CreateNewGame createNewGameHandler;
    private final JoinGame joinGameHandler;
    private final ListGames listGamesHandler;
    private final DBConfig configuration;

    public Server() {
        AuthService tempAuthService = null;
        GameService tempGameService = null;
        UserService tempUserService = null;
        DeleteAllData tempDeleteAllDataHandler = null;
        RegisterUser tempRegisterUserHandler = null;
        LoginUser tempLoginUserHandler = null;
        LogoutUser tempLogoutUserHandler = null;
        CreateNewGame tempCreateNewGameHandler = null;
        JoinGame tempJoinGameHandler = null;
        ListGames tempListGamesHandler = null;
        DBConfig tempConfiguration = null;

        try {
            UserAccess userAccess = new UserAccess();
            GameAccess gameAccess = new GameAccess(userAccess);

            SqlUserAccess sqlUserAccess = new SqlUserAccess();
            SqlAuthAccess sqlAuthAccess = new SqlAuthAccess();
            SqlGameAccess sqlGameAccess = new SqlGameAccess(sqlAuthAccess, gameAccess);

            tempAuthService = new AuthService(sqlAuthAccess);
            tempGameService = new GameService(sqlGameAccess, sqlUserAccess, sqlAuthAccess);
            tempUserService = new UserService(userAccess, sqlUserAccess);

            tempDeleteAllDataHandler = new DeleteAllData(tempAuthService, tempUserService, tempGameService);
            tempRegisterUserHandler = new RegisterUser(tempUserService, tempAuthService);
            tempLoginUserHandler = new LoginUser(tempUserService, tempAuthService);
            tempLogoutUserHandler = new LogoutUser(tempUserService);
            tempCreateNewGameHandler = new CreateNewGame(tempGameService, tempAuthService);
            tempJoinGameHandler = new JoinGame(tempGameService);
            tempListGamesHandler = new ListGames(tempGameService);
            tempConfiguration = new DBConfig();

        } catch (ResponseException | DataAccessException e) {
            System.err.println("Error initializing Server: " + e.getMessage());
            e.printStackTrace();
        }

        this.authService = tempAuthService;
        this.gameService = tempGameService;
        this.userService = tempUserService;
        this.deleteAllDataHandler = tempDeleteAllDataHandler;
        this.registerUserHandler = tempRegisterUserHandler;
        this.loginUserHandler = tempLoginUserHandler;
        this.logoutUserHandler = tempLogoutUserHandler;
        this.createNewGameHandler = tempCreateNewGameHandler;
        this.joinGameHandler = tempJoinGameHandler;
        this.listGamesHandler = tempListGamesHandler;
        this.configuration = tempConfiguration;
    }

    public int run(int desiredPort) {
        try {
            Spark.port(desiredPort);

            Spark.staticFiles.location("web");
            Spark.get("/db", (req, res) -> "Database route active!");

            // Start database here
            configuration.configureDatabase();

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
        } catch (DataAccessException e) {
            System.err.println("Error initializing the server: " + e.getMessage());
            e.printStackTrace();
            return -1; // Indicating an error
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
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

package client;

import client.apiclients.CreateGameRequest;
import exception.ResponseException;
import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private ChessClient client;
    private CreateGameRequest gameResponse;

    @BeforeEach
    public void setup() {
        client = new ChessClient("http://localhost:8080"); // Adjust if needed
    }

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @Order(3)
    public void testLoginSuccess() throws ResponseException {
        // Register a test user first (needed before login)
        String username = "testUser";
        String email = "test@example.com";
        String password = "testPass";

//        client.register(username, email, password);

        // Now attempt login with the same credentials
        String response = client.login(username, password);

        assertNotNull(response);
        assertTrue(response.contains("You signed in as testUser"));
    }

    @Test
    @Order(5)
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    @Order(4)
    public void testLoginMissingParameters() {
        ResponseException exception = assertThrows(ResponseException.class, () -> client.login("onlyUsername"));

        // Assert that the status code and message are correct
        assertEquals(400, exception.statusCode());
        assertEquals("Expected: <username> <password>", exception.getMessage());
    }


    @Test
    @Order(1)
    public void testRegisterSuccess() throws Exception {
        String username = "newUser";
        String password = "securePass123";
        String email = "newUser@example.com";

        // Assuming register() returns some kind of success message or object
        var response = client.register(username, email, password);

        assertNotNull(response);  // Ensure response isn't null
        assertTrue(response.contains("success") || response.contains(username)); // Check for expected success content
    }

    @Test
    @Order(2)
    public void testRegisterMissingParameters() {
        ResponseException exception = assertThrows(ResponseException.class, () -> client.register("onlyUsername"));

        assertEquals(400, exception.statusCode());
        assertEquals("Expected: <username> <email> <password>", exception.getMessage());

        String jsonResponse = exception.toJson();
        assertTrue(jsonResponse.contains("\"message\":\"Expected: \\u003cusername\\u003e \\u003cemail\\u003e \\u003cpassword\\u003e"));
        assertTrue(jsonResponse.contains("\"status\":400"));
    }

    @Test
    @Order(6)
    public void testCreateGameSuccess() throws Exception {
        String gameName = "TestGame";
        client.login("testUser", "testPass");

        String response = client.createGame(gameName);
        String responseString = "Game created under the name " + gameName + ".";

        assertNotNull(response);
        assertEquals(response, responseString);
    }

    @Test
    @Order(7)
    public void testLogoutSuccess() throws Exception {
        // Assuming that the user is already logged in with a valid token
        client.login("testUser", "testPass"); // or set the necessary pre-conditions

        String response = client.logout(); // Log the user out

        assertEquals("you signed out", response); // Assumes that the logout() method returns this message
    }

    @Test
    @Order(8)
    public void testLogoutWithoutLogin() throws ResponseException {
        ResponseException exception = assertThrows(ResponseException.class, () -> client.logout());

        assertEquals("Error: not authorized", exception.getMessage());

        assertEquals(401, exception.statusCode());
    }

    @Test
    @Order(9)
    public void testListGames() throws ResponseException {
        client.login("testUser", "testPass");
        client.createGame("gameName1");
        client.createGame("gameName2");

        // Get the result from the listGames method
        String result = client.listGames();

        // Assert that the result contains the correct game names
        assertTrue(result.contains("gameName1"));
        assertTrue(result.contains("gameName2"));

        // Check that the usernames are either empty or marked as N/A
        assertTrue(result.contains("Black Username:"));
        assertTrue(result.contains("White Username:"));
    }

    @Test
    @Order(10)
    public void testCreateGameMissingParameters() throws ResponseException {
        client.login("testUser", "testPass");

        ResponseException exception = assertThrows(ResponseException.class, () -> client.createGame());

        assertEquals(400, exception.statusCode());
        assertEquals("Expected: <gameName>", exception.getMessage());

        String jsonResponse = exception.toJson();
        assertTrue(jsonResponse.contains("\"status\":400"));
    }

    @Test
    @Order(11)
    public void testJoinGameSuccess() throws ResponseException {
        client.login("testUser", "testPass");

        String[] params = {"1", "BLACK"};

        String result = client.playGame(params);
        assertTrue(result.contains("joined"));

    }

    @Test
    @Order(12)
    void testObserveGameSuccess() throws ResponseException {
        String[] params = {"1"};

        assertDoesNotThrow(() -> client.observeGame(params));
    }

    @Test
    @Order(13)
    void testObserveGameWithoutGameNumber() {
        String[] params = {};
        Exception exception = assertThrows(ResponseException.class, () -> {
            client.observeGame(params); // Empty game number
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Expected: <gameNumber>"));
    }

    @Test
    @Order(14)
    void testJoinGameWithInvalidGameID() throws ResponseException {
        String[] params = {"testUser", "fancyEmail", "testPass"};
        client.register(params);
        client.login("testUser", "testPass");

        String[] params2 = {"b", "BLACK"};

        Exception exception = assertThrows(NumberFormatException.class, () -> {
            client.playGame(params2);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("For input string"));
    }

}

package client;

import client.APIClients.CreateGameRequest;
import handlers.exception.ResponseException;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

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
    public void testLoginSuccess() throws ResponseException {
        // Register a test user first (needed before login)
        String username = "testUser";
        String email = "test@example.com";
        String password = "testPass";

        client.register(username, email, password);

        // Now attempt login with the same credentials
        String response = client.login(username, password);

        assertNotNull(response);
        assertTrue(response.contains("You signed in as testUser"));
    }

    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    public void testLoginMissingParameters() {
        ResponseException exception = assertThrows(ResponseException.class, () -> client.login("onlyUsername"));

        assertEquals(400, exception.statusCode());
        assertEquals("Expected: <username> <password>", exception.getMessage());

        String jsonResponse = exception.toJson();
        System.out.println("Actual JSON Output: " + jsonResponse); // Debugging output

        String expectedJson = "{\"status\":400,\"message\":\"Expected: \\u003cusername\\u003e \\u003cpassword\\u003e\"}";
        assertEquals(expectedJson, jsonResponse);

    }

    @Test
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
    public void testRegisterMissingParameters() {
        ResponseException exception = assertThrows(ResponseException.class, () -> client.register("onlyUsername"));

        assertEquals(400, exception.statusCode());
        assertEquals("Expected: <username> <email> <password>", exception.getMessage());

        String jsonResponse = exception.toJson();
        assertTrue(jsonResponse.contains("\"message\":\"Expected: \\u003cusername\\u003e \\u003cemail\\u003e \\u003cpassword\\u003e"));
        assertTrue(jsonResponse.contains("\"status\":400"));
    }

    @Test
    public void testCreateGameSuccess() throws Exception {
        String gameName = "TestGame";
        client.login("testUser", "testPass");

        String response = client.createGame(gameName);
        String responseString = "Game created under the name " + gameName + ".";

        assertNotNull(response);
        assertEquals(response, responseString);
    }

    @Test
    public void testLogoutSuccess() throws Exception {
        // Assuming that the user is already logged in with a valid token
        client.login("testUser", "testPass"); // or set the necessary pre-conditions

        String response = client.logout(); // Log the user out

        assertEquals("you signed out", response); // Assumes that the logout() method returns this message
    }

    @Test
    public void testLogoutWithoutLogin() throws ResponseException {
        ResponseException exception = assertThrows(ResponseException.class, () -> client.logout());

        assertEquals("Error: not authorized", exception.getMessage());

        assertEquals(401, exception.statusCode());
    }

    @Test
    public void testListGames() throws ResponseException {
        client.login("testUser", "testPass");
        client.createGame("gameName1");
        client.createGame("gameName2");

        List<Map<String, Object>> gamesList = List.of(
                Map.of("1", 1, "gameName1", "Chess Game 1"),
                Map.of("2", 2, "gameName2", "Chess Game 2")
        );

        String result = client.listGames();  // This should return the formatted string like "Game list: [...]"

        // Assert that the response contains the expected "Game list:"
        assertTrue(result.contains("Game list:"));

        // Assert that both game names are in the response
        assertTrue(result.contains("gameName1"));
        assertTrue(result.contains("gameName2"));

        // Assert that the result contains both gameIDs and gameNames
        assertTrue(result.contains("gameID"));
        assertTrue(result.contains("gameName"));

        // Optionally: Checking the values are formatted as expected (this depends on the exact output format of `listGames()`)
        assertTrue(result.contains("gameID=1.0"));
        assertTrue(result.contains("gameName=gameName1"));
        assertTrue(result.contains("gameID=2.0"));
        assertTrue(result.contains("gameName=gameName2"));
    }

    @Test
    public void testCreateGameMissingParameters() throws ResponseException {
        client.login("testUser", "testPass");

        ResponseException exception = assertThrows(ResponseException.class, () -> client.createGame());

        assertEquals(400, exception.statusCode());
        assertEquals("Expected: <gameName>", exception.getMessage());

        String jsonResponse = exception.toJson();
        assertTrue(jsonResponse.contains("\"status\":400"));
    }

    @Test
    public void testJoinGameSuccess() throws ResponseException {
        client.login("testUser", "testPass");

        String[] params = {"1", "BLACK"};

        String result = client.playGame(params);
        assertTrue(result.contains("joined"));

    }

    @Test
    void testObserveGameSuccess() throws ResponseException {
        String[] params = {"1"};

        assertDoesNotThrow(() -> client.observeGame(params));
    }

    @Test
    void testObserveGameWithoutGameNumber() {
        String[] params = {};
        Exception exception = assertThrows(ResponseException.class, () -> {
            client.observeGame(params); // Empty game number
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Expected: <gameNumber>"));
    }

    @Test
    void testJoinGameWithInvalidGameID() throws ResponseException {
        client.login("testUser", "testPass");

        String[] params = {"b", "BLACK"};

        Exception exception = assertThrows(NumberFormatException.class, () -> {
            client.playGame(params);
        });

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("For input string"));
    }

}

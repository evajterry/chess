package client;

import client.APIClients.CreateGameRequest;
import handlers.exception.ResponseException;
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

        String expectedJson = "{\"message\":\"Expected: \\u003cusername\\u003e \\u003cpassword\\u003e\",\"status\":400}";
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



}

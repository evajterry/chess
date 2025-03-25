package dataaccess;

import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Ensures tests run in order
class DBConfigTest {
    private static DBConfig dbConfig;
    private static SqlGameAccess sqlGameAccess;
    private static SqlAuthAccess sqlAuthAccess; // You should instantiate or mock this
    private static GameAccess gameAccess;
    private static UserAccess userAccess;

    @BeforeAll
    static void setUp() throws ResponseException, DataAccessException {

        dbConfig = new DBConfig();
        dbConfig.configureDatabase();

        // Instantiate your dependencies
        sqlAuthAccess = new SqlAuthAccess(); // Assuming this is a real implementation
        userAccess = new UserAccess();
        gameAccess = new GameAccess(userAccess); // Assuming this is a real implementation or mock

        // Create the SqlGameAccess instance
        sqlGameAccess = new SqlGameAccess(sqlAuthAccess, gameAccess);
    }

    @AfterAll
    static void tearDown() throws ResponseException, DataAccessException {
        String[] deleteStatements = {
                "DROP TABLE IF EXISTS AuthData;",
                "DROP TABLE IF EXISTS GameData;",
                "DROP TABLE IF EXISTS UserData;"
        };
//        dbConfig.configureDatabase();
    }

    @Test
    @Order(1)
    void testConfigureDatabase() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'AuthData'")) {

            assertTrue(rs.next(), "Table AuthData should exist");
        } catch (SQLException | DataAccessException e) {
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testExecuteUpdate() {
        try {
            String insertStatement = "INSERT INTO AuthData (authToken, username, json) VALUES (?, ?, ?)";
            int generatedId = dbConfig.executeUpdate(insertStatement, "testAuthToken", "testUser", "{}");

            assertTrue(generatedId > 0, "Generated ID should be positive");

            // Verify data was inserted
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM AuthData WHERE id = " + generatedId)) {

                assertTrue(rs.next(), "Inserted record should exist");
                assertEquals("testUser", rs.getString("username"));
            }

        } catch (ResponseException | DataAccessException | SQLException e) {
            fail("Database update failed: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void testRegisterUser() {
        try {
            SqlUserAccess userAccess = new SqlUserAccess();
            UserData testUser = new UserData("testUser", "test@example.com", "password123");

            String authToken = userAccess.registerUser(testUser);

            assertNotNull(authToken, "Auth token should not be null");

            // Verify the user was inserted
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM UserData WHERE username = ?")) {
                ps.setString(1, "testUser");

                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "User should exist in the database");
                    assertEquals("test@example.com", rs.getString("email"), "Email should match");
                }
            }
        } catch (DataAccessException | SQLException e) {
            fail("User registration failed: " + e.getMessage());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(5)
    void testLoginUserSuccess() {
        try {
            SqlUserAccess userAccess = new SqlUserAccess();
            UserData testUser = new UserData("testUser", "test@example.com", "password123");
            userAccess.registerUser(testUser);

            String returnedAuthToken = userAccess.loginUser(testUser);

            assertNotNull(returnedAuthToken, "Auth token should not be null");

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM AuthData WHERE authToken = ?")) {
                ps.setString(1, returnedAuthToken);

                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Auth token should exist in the database");
                    assertEquals(returnedAuthToken, rs.getString("authToken"), "Inserted auth token should match");
                }
            }
        } catch (DataAccessException | SQLException e) {
            fail("Login user test failed: " + e.getMessage());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(6)
    void testLoginUserUnauthorized() {
        try {
            SqlUserAccess userAccess = new SqlUserAccess();
            UserData nonExistentUser = new UserData("nonExistentUser", "nonexistent@example.com", "wrongpassword");
            String result = userAccess.loginUser(nonExistentUser);
            assertEquals("Error: unauthorized", result, "Login should fail for a non-existent user");

        } catch (DataAccessException e) {
            fail("Login user test failed: " + e.getMessage());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(7)
    void testUserExistsUserExists() {
        try {
            SqlUserAccess userAccess = new SqlUserAccess();
            UserData testUser = new UserData("testUser", "test@example.com", "password123");
            userAccess.registerUser(testUser);

            assertTrue(userAccess.userExists(testUser), "User should exist in the database");

        } catch (DataAccessException e) {
            fail("User existence check failed: " + e.getMessage());
        } catch (ResponseException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(8)
    void testUserExistsUserDoesNotExist() {
        try {
            SqlUserAccess userAccess = new SqlUserAccess();
            UserData testUser = new UserData("nonExistentUser", "nonexistent@example.com", "password123");

            assertFalse(userAccess.userExists(testUser), "User should not exist in the database");

        } catch (DataAccessException e) {
            fail("User existence check failed: " + e.getMessage());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    @Order(11)
    void testJoinNewGameSuccess() {
        try {
            SqlUserAccess sqlUserAccess = new SqlUserAccess();
            SqlAuthAccess sqlAuthAccess = new SqlAuthAccess();
            SqlGameAccess sqlGameAccess = new SqlGameAccess(sqlAuthAccess, gameAccess);

            String authToken = "valid-auth-token";
            String requestedTeam = "WHITE";

            // Insert auth data
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO AuthData (authToken, username) VALUES (?, ?)")) {
                ps.setString(1, authToken);
                ps.setString(2, "testUser");
                ps.executeUpdate();
            }

            // Create a new game and retrieve the generated gameID
            int gameID;
            try (Connection conn = DatabaseManager.getConnection()) {
                gameID = Integer.parseInt(sqlGameAccess.createNewGame(authToken, "game name"));
            }

            // Now, verify the game is created
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM GameData WHERE gameID = ?")) {
                ps.setInt(1, gameID);
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next(), "Game should exist in the database");

                // Verify the game is correctly inserted
                assertNull(rs.getString("whiteUsername"), "White username should be null initially");
                assertNull(rs.getString("blackUsername"), "Black username should be null initially");
            }

            // Now try to join the game
            boolean result = sqlGameAccess.joinNewGame(authToken, gameID, requestedTeam);

            assertTrue(result, "User should successfully join the game");

            // Verify the player joined the game
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM GameData WHERE gameID = ?")) {
                ps.setInt(1, gameID);
                ResultSet rs = ps.executeQuery();
                assertTrue(rs.next(), "Game should exist in the database");

                if ("WHITE".equals(requestedTeam)) {
                    assertEquals("testUser", rs.getString("whiteUsername"), "White username should be testUser");
                } else if ("BLACK".equals(requestedTeam)) {
                    assertEquals("testUser", rs.getString("blackUsername"), "Black username should be testUser");
                }
            }
        } catch (SQLException | DataAccessException e) {
            fail("Test failed: " + e.getMessage());
        } catch (ResponseException e) {
            fail("ResponseException: " + e.getMessage());
        }
    }

}

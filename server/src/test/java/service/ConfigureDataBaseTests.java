package service;

import handlers.exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;
import java.util.Map;

import dataaccess.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Ensures tests run in order
class DBConfigTest {
    private static DBConfig dbConfig;
    private static SqlAuthAccess authAccess;

    @BeforeAll
    static void setUp() throws ResponseException, DataAccessException {
        dbConfig = new DBConfig();
        dbConfig.configureDatabase();
    }

    @AfterAll
    static void tearDown() throws ResponseException, DataAccessException {
        String[] deleteStatements = {
                "DELETE FROM AuthTokens WHERE user_id IN (SELECT id FROM UserData);",
                "DROP TABLE IF EXISTS AuthData;",
                "DROP TABLE IF EXISTS GameData;",
                "DROP TABLE IF EXISTS UserData;"
        };
        dbConfig.configureDatabase();
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
    void testUserExists_UserExists() {
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
    void testUserExists_UserDoesNotExist() {
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
    @Order(9)
    void testListGames() {
        try {
            try (Connection conn = DatabaseManager.getConnection()) {
                // Insert game data
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO GameData (whiteUsername, blackUsername, gameName) VALUES (?, ?, ?)")) {
                    ps.setString(1, "player1");
                    ps.setString(2, "player2");
                    ps.setString(3, "Test Game");
                    ps.executeUpdate();
                }

                // Insert auth data
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO AuthData (authToken, username) VALUES (?, ?)")) {
                    ps.setString(1, "valid-auth-token");
                    ps.setString(2, "testUser");
                    ps.executeUpdate();
                }
            }

            // Call the method under test
            SqlUserAccess sqlUserAccess = new SqlUserAccess();
            SqlAuthAccess sqlAuthAccess = new SqlAuthAccess();
            SqlGameAccess sqlGameAccess = new SqlGameAccess(sqlUserAccess, sqlAuthAccess);
            List<Map<String, Object>> gamesList = sqlGameAccess.listGames("valid-auth-token");

            // Assertions
            assertNotNull(gamesList, "Games list should not be null");
            assertEquals(1, gamesList.size(), "Should return one game");

            Map<String, Object> game = gamesList.get(0);
            assertEquals(1, game.get("gameID"), "Game ID should match");
            assertEquals("player1", game.get("whiteUsername"), "White username should match");
            assertEquals("player2", game.get("blackUsername"), "Black username should match");
            assertEquals("Test Game", game.get("gameName"), "Game name should match");

        } catch (SQLException | DataAccessException e) {
            fail("listGames test failed: " + e.getMessage());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(10)
    void testListTwoGames() {
        try {
            try (Connection conn = DatabaseManager.getConnection()) {
                // Insert first game data
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO GameData (whiteUsername, blackUsername, gameName) VALUES (?, ?, ?)")) {
                    ps.setString(1, "player1");
                    ps.setString(2, "player2");
                    ps.setString(3, "Test Game 1");
                    ps.executeUpdate();
                }

                // Insert second game data
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO GameData (whiteUsername, blackUsername, gameName) VALUES (?, ?, ?)")) {
                    ps.setString(1, "player3");
                    ps.setString(2, "player4");
                    ps.setString(3, "Test Game 2");
                    ps.executeUpdate();
                }

                // Insert auth data
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO AuthData (authToken, username) VALUES (?, ?)")) {
                    ps.setString(1, "valid-auth-token");
                    ps.setString(2, "testUser");
                    ps.executeUpdate();
                }
            }

            // Call the method under test
            SqlUserAccess sqlUserAccess = new SqlUserAccess();
            SqlAuthAccess sqlAuthAccess = new SqlAuthAccess();
            SqlGameAccess sqlGameAccess = new SqlGameAccess(sqlUserAccess, sqlAuthAccess);
            List<Map<String, Object>> gamesList = sqlGameAccess.listGames("valid-auth-token");

            // Assertions
            assertNotNull(gamesList, "Games list should not be null");
            assertEquals(2, gamesList.size(), "Should return two games");

            // Assert for the first game
            Map<String, Object> game1 = gamesList.get(0);
            assertEquals(1, game1.get("gameID"), "First game ID should match");
            assertEquals("player1", game1.get("whiteUsername"), "First game white username should match");
            assertEquals("player2", game1.get("blackUsername"), "First game black username should match");
            assertEquals("Test Game 1", game1.get("gameName"), "First game name should match");

            // Assert for the second game
            Map<String, Object> game2 = gamesList.get(1);
            assertEquals(2, game2.get("gameID"), "Second game ID should match");
            assertEquals("player3", game2.get("whiteUsername"), "Second game white username should match");
            assertEquals("player4", game2.get("blackUsername"), "Second game black username should match");
            assertEquals("Test Game 2", game2.get("gameName"), "Second game name should match");

        } catch (SQLException | DataAccessException e) {
            fail("testListTwoGames failed: " + e.getMessage());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(11)
    void testJoinNewGame_Success() {
        try {
            SqlUserAccess sqlUserAccess = new SqlUserAccess();
            SqlAuthAccess sqlAuthAccess = new SqlAuthAccess();
            SqlGameAccess sqlGameAccess = new SqlGameAccess(sqlUserAccess, sqlAuthAccess);

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
    @Test
    @Order(12)
    void testDeleteAllData() {
        try {
            // Insert test data into multiple tables
            dbConfig.executeUpdate("INSERT INTO AuthData (authToken, username, json) VALUES (?, ?, ?)", "testToken", "testUser", "{}");
            dbConfig.executeUpdate("INSERT INTO UserData (username, email, password, json) VALUES (?, ?, ?, ?)", "testUser", "test@example.com", "password123", "{}");
            dbConfig.executeUpdate("INSERT INTO GameData (whiteUsername, blackUsername, gameID, gameName, game) VALUES (?, ?, ?, ?, ?)", "player1", "player2", 2, "Test Game", "Game data");

            // Call your function to delete all data
            dbConfig.deleteAllData(); // Ensure this function exists in DBConfig

            // Verify tables are empty
            try (Connection conn = DatabaseManager.getConnection()) {
                assertTrue(isTableEmpty(conn, "AuthData"), "AuthData should be empty after deletion");
                assertTrue(isTableEmpty(conn, "UserData"), "UserData should be empty after deletion");
                assertTrue(isTableEmpty(conn, "GameData"), "GameData should be empty after deletion");
            }
        } catch (ResponseException | DataAccessException | SQLException e) {
            fail("Delete all data test failed: " + e.getMessage());
        }
    }

    // Helper method to check if a table is empty
    private boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return false;
    }

}

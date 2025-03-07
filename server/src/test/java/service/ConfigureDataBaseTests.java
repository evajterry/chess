package service;

import handlers.exception.ResponseException;
import org.junit.jupiter.api.*;

import java.sql.*;

import dataaccess.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Ensures tests run in order
class DBConfigTest {
    private static DBConfig dbConfig;
    private static SqlAuthAccess authAccess;

    @BeforeAll
    static void setUp() throws ResponseException, DataAccessException {
        dbConfig = new DBConfig();
        String[] createStatements = {
                "DROP TABLE IF EXISTS AuthData;",
                """
                CREATE TABLE AuthData (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    authToken VARCHAR(256) NOT NULL,
                    username VARCHAR(256) NOT NULL,
                    json TEXT DEFAULT NULL
                ) ENGINE=InnoDB;
                """
        };
        dbConfig.configureDatabase(createStatements);
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
    @Order(3)
    void testInsertAuthToken() {
        try {
            try {
                authAccess = new SqlAuthAccess();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize authAccess", e);
            }
            String authToken = "test-token";
            authAccess.insertAuthToken(authToken);

            // Verify the token was inserted
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT authToken FROM AuthData WHERE authToken = ?")) {
                ps.setString(1, authToken);

                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Auth token should exist in the database");
                    assertEquals(authToken, rs.getString("authToken"), "Inserted auth token should match");
                }
            }
        } catch (ResponseException | DataAccessException | SQLException e) {
            fail("Auth token insertion failed: " + e.getMessage());
        }
    }
}

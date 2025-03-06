package service;

import handlers.exception.ResponseException;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dataaccess.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Ensures tests run in order
class DBConfigTest {
    private static DBConfig dbConfig;

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
}

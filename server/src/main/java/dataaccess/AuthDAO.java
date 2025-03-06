package dataaccess;

public interface AuthDAO {
    void insertAuthToken(String authToken);
    void deleteAllData();
}

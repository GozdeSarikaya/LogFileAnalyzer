package Exception;

public class DatabaseException extends RuntimeException {
    public DatabaseException(Exception e) {
        super(e);
    }
}

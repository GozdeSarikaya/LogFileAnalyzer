package Exception;

public class DatabaseInsertException extends RuntimeException {
    public DatabaseInsertException(Exception e) {
        super(e);
    }
}

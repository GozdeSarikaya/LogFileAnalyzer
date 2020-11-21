package Exception;

public class LogFileProcessException extends RuntimeException {
    public LogFileProcessException(Exception e) {
        super(e);
    }
}

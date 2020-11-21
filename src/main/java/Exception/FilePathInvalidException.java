package Exception;

public class FilePathInvalidException extends RuntimeException {
    public FilePathInvalidException(Exception e) {
        super(e);
    }
}

package Exception;

public class ParameterInvalidException extends RuntimeException {
    public ParameterInvalidException(Exception e) {
        super(e);
    }
}

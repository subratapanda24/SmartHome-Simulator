package exception;

public class PowerLimitExceededException extends Exception {
    public PowerLimitExceededException(String message) {
        super(message);
    }
}

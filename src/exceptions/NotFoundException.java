package exceptions;

public class NotFoundException extends Error {
    public NotFoundException(final String message) {
        super(message);
    }
}

package no.unit.nva.exceptions.InvalidAccessRightException;

public class InvalidAccessRightException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "Invalid Access right: ";

    public InvalidAccessRightException(String accessRight) {
        super(DEFAULT_MESSAGE + accessRight);
    }
}

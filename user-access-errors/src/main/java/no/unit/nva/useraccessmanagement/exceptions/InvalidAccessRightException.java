package no.unit.nva.useraccessmanagement.exceptions;

public class InvalidAccessRightException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "Invalid Access right: ";

    public InvalidAccessRightException(String accessRight) {
        super(DEFAULT_MESSAGE + accessRight);
    }
}

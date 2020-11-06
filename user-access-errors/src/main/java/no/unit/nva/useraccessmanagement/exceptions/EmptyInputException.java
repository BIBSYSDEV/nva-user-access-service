package no.unit.nva.useraccessmanagement.exceptions;

import no.unit.nva.useraccessmanagement.exceptions.InvalidInputException;

public class EmptyInputException extends InvalidInputException {

    public EmptyInputException(String message) {
        super(message);
    }
}

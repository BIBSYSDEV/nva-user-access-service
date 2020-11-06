package no.unit.nva.exceptions;

import no.unit.nva.useraccessmanagement.model.exceptions.InvalidInputException;

public class EmptyInputException extends InvalidInputException {

    public EmptyInputException(String message) {
        super(message);
    }
}

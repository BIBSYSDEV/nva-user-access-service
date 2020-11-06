package no.unit.nva.handlers;

import static nva.commons.utils.attempt.Try.attempt;

import java.util.Optional;
import java.util.concurrent.Callable;

import no.unit.nva.useraccessmanagement.dao.exceptions.InvalidEntryInternalException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.utils.Environment;
import nva.commons.utils.attempt.Failure;
import org.slf4j.Logger;

public abstract class HandlerWithEventualConsistency<I, O> extends ApiGatewayHandler<I, O> {

    public static final String FAILED_TO_FETCH_OBJECT = "Failed to fetch object.Effort %s/%s";
    protected static final int MAX_EFFORTS_FOR_FETCHING_OBJECT = 2;
    protected static final String INTERRUPTION_ERROR = "Interuption while waiting to get role.";
    protected static final long WAITING_TIME = 100;
    public static final String INVALID_ENTRY_IN_DATABASE = "Saved invalid entry in database.";

    protected HandlerWithEventualConsistency(Class<I> iclass, Environment environment, Logger logger) {
        super(iclass, environment, logger);
    }

    protected Optional<O> getEventuallyConsistent(Callable<O> fetchEntry) {
        int counter = 0;

        Optional<O> eventuallyConsistentObject = tryFetchingEntry(fetchEntry, counter);
        while (eventuallyConsistentObject.isEmpty() && counter < MAX_EFFORTS_FOR_FETCHING_OBJECT) {
            waitForEventualConsistency();
            eventuallyConsistentObject = tryFetchingEntry(fetchEntry, counter);
            counter++;
        }
        return eventuallyConsistentObject;
    }

    private Optional<O> tryFetchingEntry(Callable<O> tryGetObject, final int counter) {
        return attempt(tryGetObject).toOptional(fail -> logMessage(fail, counter));
    }

    private <S> void logMessage(Failure<S> failure, int counter) {
        if (failure.getException() instanceof InvalidEntryInternalException) {
            logger.error(INVALID_ENTRY_IN_DATABASE, failure.getException());
        } else {
            logger.debug(String.format(FAILED_TO_FETCH_OBJECT, counter, MAX_EFFORTS_FOR_FETCHING_OBJECT));
        }
    }

    private void waitForEventualConsistency() {
        try {
            Thread.sleep(WAITING_TIME);
        } catch (InterruptedException e) {
            logger.error(INTERRUPTION_ERROR, e);
            throw new RuntimeException(INTERRUPTION_ERROR, e);
        }
    }
}

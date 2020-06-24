package no.unit.nva.handlers;

import java.util.Optional;
import java.util.function.Supplier;
import no.unit.nva.exceptions.UnexpectedException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.utils.Environment;
import org.slf4j.Logger;

public abstract class HandlerWithEventualConsistency<I,O> extends ApiGatewayHandler<I,O> {

    protected static final int MAX_EFFORTS_FOR_FETCHING_OBJECT = 2;
    protected static final String INTERRUPTION_ERROR = "Interuption while waiting to get role.";
    protected static final long WAITING_TIME = 100;

    public HandlerWithEventualConsistency(Class<I> iclass, Logger logger) {
        super(iclass, logger);
    }

    public HandlerWithEventualConsistency(Class<I> iclass, Environment environment, Logger logger) {
        super(iclass, environment, logger);
    }

    protected Optional<O> getEventuallyConsistent(Supplier<Optional<O>> tryGetObject) throws UnexpectedException {
        Optional<O> eventuallyConsistentObject = tryGetObject.get();
        int counter = 0;
        while (eventuallyConsistentObject.isEmpty() && counter < MAX_EFFORTS_FOR_FETCHING_OBJECT) {
            waitForEventualConsistency();
            eventuallyConsistentObject = tryGetObject.get();
            counter++;
        }
        return eventuallyConsistentObject;
    }

    private void waitForEventualConsistency() throws UnexpectedException {
        try {
            Thread.sleep(WAITING_TIME);
        } catch (InterruptedException e) {
            logger.error(INTERRUPTION_ERROR, e);
            throw new UnexpectedException(INTERRUPTION_ERROR, e);
        }
    }

    protected RuntimeException unexpectedFailure(String message,Exception exception) {
        logger.error(message);
        return new RuntimeException(message, exception);
    }




}

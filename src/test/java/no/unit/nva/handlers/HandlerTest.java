package no.unit.nva.handlers;

import static nva.commons.utils.JsonUtils.objectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import no.unit.nva.database.DatabaseAccessor;
import no.unit.nva.model.TypedObjectsDetails;
import no.unit.nva.testutils.HandlerRequestBuilder;

public abstract class HandlerTest extends DatabaseAccessor {

    protected <T> InputStream createRequestInputStream(T bodyObject)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<T>(objectMapper)
            .withBody(bodyObject)
            .build();
    }

    protected <I> ObjectNode createInputObjectWithoutType(I dtoObject) {
        ObjectNode objectWithoutType = objectMapper.convertValue(dtoObject, ObjectNode.class);
        objectWithoutType.remove(TypedObjectsDetails.TYPE_ATTRIBUTE);
        return objectWithoutType;
    }
}

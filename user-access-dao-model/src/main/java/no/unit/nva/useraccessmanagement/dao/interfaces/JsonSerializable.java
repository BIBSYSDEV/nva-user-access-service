package no.unit.nva.useraccessmanagement.dao.interfaces;

import static nva.commons.utils.attempt.Try.attempt;
import com.fasterxml.jackson.databind.ObjectMapper;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.attempt.Failure;

public interface JsonSerializable {

    default String toJsonString() {
        return toJsonString(JsonUtils.objectMapper);
    }

    /**
     * a JSON representation of the object.
     *
     * @return a JSON representation of the object.
     */
    default String toJsonString(ObjectMapper objectMapper) {
        return
            attempt(() -> objectMapper.writeValueAsString(this))
                .orElseThrow(this::newUnexpectedException);
    }

    private RuntimeException newUnexpectedException(Failure<String> fail) {
        return new RuntimeException(fail.getException());
    }
}

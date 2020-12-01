package no.unit.nva.useraccessmanagement.interfaces;

import static nva.commons.utils.attempt.Try.attempt;
import com.fasterxml.jackson.databind.ObjectMapper;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.JsonUtils;
import nva.commons.utils.attempt.Failure;

public interface JsonSerializable {

    @JacocoGenerated
    default String toJsonString() {
        return toJsonString(JsonUtils.objectMapper);
    }

    /**
     * a JSON representation of the object.
     *
     * @return a JSON representation of the object.
     */
    @JacocoGenerated
    default String toJsonString(ObjectMapper objectMapper) {
        return
            attempt(() -> objectMapper.writeValueAsString(this))
                .orElseThrow(this::newUnexpectedException);
    }

    @JacocoGenerated
    private RuntimeException newUnexpectedException(Failure<String> fail) {
        return new RuntimeException(fail.getException());
    }
}

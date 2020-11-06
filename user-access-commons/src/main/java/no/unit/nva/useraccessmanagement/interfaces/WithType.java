package no.unit.nva.useraccessmanagement.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.utils.JacocoGenerated;

public interface WithType {

    @JsonProperty("type")
    String getType();

    @JacocoGenerated
    default void setType(String type) {
        // Do nothing.
    }
}

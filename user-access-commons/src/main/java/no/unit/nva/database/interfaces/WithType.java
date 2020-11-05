package no.unit.nva.database.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface WithType {

    @JsonProperty("type")
    String getType();

    default void setType(String type) {
        // Do nothing;
    }
}

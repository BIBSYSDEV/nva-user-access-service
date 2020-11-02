package no.unit.nva.database.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface WithType {

    @JsonProperty("type")
    default String getType() {
        return this.getClass().getSimpleName();
    }

    default void setType(String type) {
        //
    }
}

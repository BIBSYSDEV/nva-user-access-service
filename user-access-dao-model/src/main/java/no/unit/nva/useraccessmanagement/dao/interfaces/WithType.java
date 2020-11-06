package no.unit.nva.useraccessmanagement.dao.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface WithType {

    @JsonProperty(value = "type")
    String getType();

    default void setType(String type) {

    }
}

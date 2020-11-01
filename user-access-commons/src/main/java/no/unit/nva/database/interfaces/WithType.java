package no.unit.nva.database.interfaces;

public interface WithType {

    default String getType() {
        return this.getClass().getSimpleName();
    }

    ;

    default void setType(String type) {
        //Do nothing;
    }
}

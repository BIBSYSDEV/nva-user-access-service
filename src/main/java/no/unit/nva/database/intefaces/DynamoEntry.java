package no.unit.nva.database.intefaces;

import static java.util.Objects.isNull;

public abstract class DynamoEntry {

    @SuppressWarnings("PMD.ConstantsInInterface")
    public static String FIELD_DELIMITER = "#";

    public abstract String getPrimaryHashKey();

    public abstract String getPrimaryRangeKey();

    protected boolean primaryKeyHasNotBeenSet() {
        return isNull(getPrimaryHashKey());
    }

    /**
     * Do not use. Intented only for use from DynamoDB. This method has no effect as the type is always ROLE.
     *
     * @param type ignored parameter.
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public final void setType(String type) {
        // DO NOTHING
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public final void setPrimaryRangeKey(String primaryRangeKey) {
        // DO NOTHING.
    }
}

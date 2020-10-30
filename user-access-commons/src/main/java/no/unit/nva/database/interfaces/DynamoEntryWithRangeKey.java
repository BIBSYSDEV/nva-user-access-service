package no.unit.nva.database.interfaces;

import static java.util.Objects.isNull;
import no.unit.nva.exceptions.InvalidEntryInternalException;

public abstract class DynamoEntryWithRangeKey implements WithType {

    @SuppressWarnings("PMD.ConstantsInInterface")
    public static String FIELD_DELIMITER = "#";

    public abstract String getPrimaryHashKey();

    public abstract String getPrimaryRangeKey();

    public abstract void setPrimaryRangeKey(String primaryRangeKey) throws InvalidEntryInternalException;

    @Override
    public abstract String getType();

    /**
     * Do not use. Intented only for use from DynamoDB. This method has no effect as the type is always ROLE.
     *
     * @param type ignored parameter.
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public final void setType(String type) {
        // DO NOTHING
    }

    protected boolean primaryHashKeyHasNotBeenSet() {
        return isNull(getPrimaryHashKey());
    }

    protected boolean primaryRangeKeyHasNotBeenSet() {
        return isNull(getPrimaryRangeKey());
    }
}

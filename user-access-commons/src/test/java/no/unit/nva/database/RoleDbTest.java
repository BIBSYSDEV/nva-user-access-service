package no.unit.nva.database;

import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_HASH_KEY;
import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_RANGE_KEY;
import static no.unit.nva.hamcrest.DoesNotHaveNullOrEmptyFields.doesNotHaveNullOrEmptyFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import java.util.Collections;
import java.util.Set;
import no.unit.nva.database.RoleDb.Builder;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class RoleDbTest extends DatabaseAccessor {

    public static final String SOME_ROLE_NAME = "someRoleName";
    public static final String SOME_OTHER_RANGE_KEY = "SomeOtherRangeKey";
    public static final String SOME_TYPE = "SomeType";
    private final RoleDb sampleRole = createSampleRole();
    private Table table;

    public RoleDbTest() throws InvalidEntryInternalException {
    }

    @Test
    public void getPrimaryHashKeyReturnsStringContainingTypeRole() {
        assertThat(sampleRole.getPrimaryHashKey(), containsString(RoleDb.TYPE));
    }

    @Test
    public void equalsComparesAllFields() throws InvalidEntryInternalException {
        RoleDb left = sampleRole;
        RoleDb right = sampleRole.copy().build();
        assertThat(sampleRole, doesNotHaveNullOrEmptyFields());
        assertThat(left, is(equalTo(right)));
    }

    @Test
    public void equalsReturnsFalseWhenNameIsDifferent() throws InvalidEntryInternalException {
        RoleDb left = sampleRole;
        RoleDb right = sampleRole.copy().withName("SomeOtherName").build();

        assertThat(left, is(not(equalTo(right))));
    }

    @Test
    public void equalsReturnsFalseWhenAccessRightListIsDifferent() throws InvalidEntryInternalException {

        Set<AccessRight> differentAccessRights = Collections.singleton(AccessRight.REJECT_DOI_REQUEST);
        assertThat(sampleRole.getAccessRights().containsAll(differentAccessRights), is(equalTo(false)));
        RoleDb differentRole = sampleRole.copy().withAccessRights(differentAccessRights).build();

        assertThat(sampleRole, is(not(equalTo(differentRole))));
    }

    @Test
    public void roleDbHasListOfAccessRights() {
        assertThat(sampleRole.getAccessRights(), is(not(nullValue())));
    }

    @Test
    public void roleDbWithAccessRightsIsSavedInDatabase() throws InvalidEntryInternalException {
        var accessRights = Set.of(AccessRight.APPROVE_DOI_REQUEST, AccessRight.REJECT_DOI_REQUEST);
        RoleDb roleWithAccessRights = sampleRole.copy().withAccessRights(accessRights).build();
        table.putItem(roleWithAccessRights.toItem());

        Item savedRoleItem = fetchRole(roleWithAccessRights);
        RoleDb savedRole = RoleDb.fromItem(savedRoleItem, RoleDb.class);
        assertThat(savedRole, is(equalTo(roleWithAccessRights)));
    }

    @BeforeEach
    void init() {
        table = DatabaseServiceImpl.createTable(initializeTestDatabase(), envWithTableName);
    }

    @Test
    void roleDbHasBuilder() {
        RoleDb.Builder builder = RoleDb.newBuilder();
        assertNotNull(builder);
    }

    @Test
    void roleDbHasRoleName() throws InvalidEntryInternalException {
        RoleDb roleDb = createSampleRole();
        assertThat(roleDb.getName(), is(equalTo(SOME_ROLE_NAME)));
    }

    @Test
    void builderSetsTheRolename() throws InvalidEntryInternalException {
        RoleDb role = RoleDb.newBuilder().withName(SOME_ROLE_NAME).build();
        assertThat(role.getName(), is(equalTo(SOME_ROLE_NAME)));
    }

    @Test
    void buildReturnsObjectWithInitializedPrimaryHashKey() throws InvalidEntryInternalException {
        RoleDb role = RoleDb.newBuilder().withName(SOME_ROLE_NAME).build();
        assertThat(role.getPrimaryHashKey(), is(not(nullValue())));
        assertThat(role.getPrimaryHashKey(), is(not(emptyString())));
    }

    @Test
    void buildWithoutRoleNameShouldThrowException() {
        Executable action = () -> RoleDb.newBuilder().build();
        assertThrows(InvalidEntryInternalException.class, action);
    }

    @Test
    void roleDbRoleNameIsSavedInDatabase() {
        table.putItem(Item.fromJSON(sampleRole.toJsonString()));
        Item item = fetchRole(sampleRole);
        RoleDb retrievedRole = RoleDb.fromItem(item, RoleDb.class);
        assertThat(retrievedRole, is(equalTo(sampleRole)));
    }

    @Test
    void getPrimaryHashKeyReturnsStringContainingRoleName() {
        assertThat(sampleRole.getPrimaryHashKey(), containsString(sampleRole.getName()));
    }

    @Test
    void setPrimaryHashKeyShouldNotChangeTheValueOfAlreadySetPrimaryHashKey() throws InvalidEntryInternalException {
        String someOtherHashKey = "SomeOtherHashKey";
        sampleRole.setPrimaryHashKey(someOtherHashKey);
        assertThat(sampleRole.getPrimaryHashKey(), is(not(equalTo(someOtherHashKey))));
        assertThat(sampleRole.getPrimaryHashKey(), containsString(sampleRole.getName()));
        assertThat(sampleRole.getPrimaryHashKey(), containsString(RoleDb.TYPE));
    }

    @ParameterizedTest(name = "setPrimaryHashKey throws exception when input is:\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n", "\r"})
    void setPrimaryHashKeyThrowsExceptionWhenInputIsBlankOrNullString(String blankString) {
        Executable action = () -> RoleDb.newBuilder().withName(blankString).build();
        InvalidEntryInternalException exception = assertThrows(InvalidEntryInternalException.class, action);
        assertThat(exception.getMessage(), containsString(Builder.EMPTY_ROLE_NAME_ERROR));
    }

    @Test
    void setPrimaryRangeKeyHasNoEffect() throws InvalidEntryInternalException {
        RoleDb originalRole = RoleDb.newBuilder().withName(SOME_ROLE_NAME).build();
        RoleDb copy = originalRole.copy().build();
        copy.setPrimaryRangeKey(SOME_OTHER_RANGE_KEY);
        assertThat(originalRole, is(equalTo(copy)));
    }

    @Test
    void setTypeHasNoEffect() throws InvalidEntryInternalException {
        RoleDb originalRole = RoleDb.newBuilder().withName(SOME_ROLE_NAME).build();
        RoleDb copy = originalRole.copy().build();
        copy.setType(SOME_TYPE);
        assertThat(originalRole, is(equalTo(copy)));
    }

    @Test
    void copyReturnsBuilderContainingAllFieldValuesOfOriginalItem() throws InvalidEntryInternalException {
        RoleDb copyRole = sampleRole.copy().build();
        assertThat(copyRole, is(equalTo(sampleRole)));
        assertThat(copyRole, is(not(sameInstance(sampleRole))));
    }

    private Item fetchRole(RoleDb role) {
        return table.getItem(PRIMARY_KEY_HASH_KEY, role.getPrimaryHashKey(),
            PRIMARY_KEY_RANGE_KEY, role.getPrimaryRangeKey());
    }

    private RoleDb createSampleRole() throws InvalidEntryInternalException {
        Set<AccessRight> accessRights = Collections.singleton(AccessRight.APPROVE_DOI_REQUEST);
        return RoleDb.newBuilder()
            .withName(SOME_ROLE_NAME)
            .withAccessRights(accessRights)
            .build();
    }
}
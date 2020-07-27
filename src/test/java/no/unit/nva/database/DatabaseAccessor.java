package no.unit.nva.database;

import static java.util.Objects.nonNull;
import static no.unit.nva.database.DatabaseServiceImpl.SEARCH_USERS_BY_INSTITUTION_INDEX_NAME;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import java.util.ArrayList;
import java.util.List;
import no.unit.nva.database.intefaces.WithEnvironment;
import nva.commons.utils.Environment;
import org.junit.jupiter.api.AfterEach;

public abstract class DatabaseAccessor implements WithEnvironment {

    public static final String USERS_AND_ROLES_TABLE = "UsersAndRolesTable";
    public static final String HASH_KEY_NAME = "PrimaryKeyHashKey";
    public static final String SORT_KEY_NAME = "PrimaryKeyRangeKey";

    public static final String SECONDARY_INDEX_1_HASH_KEY = "SecondaryIndex1HashKey";
    public static final String SECONDARY_INDEX_1_RANGE_KEY = "SecondaryIndex1RangeKey";
    public static final int SINGLE_TABLE_EXPECTED = 1;
    private static final Long CAPACITY_DOES_NOT_MATTER = 1000L;

    protected final Environment envWithTableName = mockEnvironment(USERS_AND_ROLES_TABLE);
    protected AmazonDynamoDB localDynamo;

    public DatabaseServiceImpl createDatabaseServiceUsingLocalStorage() {
        return new DatabaseServiceImpl(initializeTestDatabase(), envWithTableName);
    }

    /**
     * Initializes a local database. The client is stored in the {@code localDynamo variable}
     *
     * @return a client connected to the local database
     */
    public AmazonDynamoDB initializeTestDatabase() {

        localDynamo = createLocalDynamoDbMock();
        String tableName = readTablenNameFronEnvironment();
        CreateTableResult createTableResult = createTable(localDynamo, tableName);
        TableDescription tableDescription = createTableResult.getTableDescription();
        assertEquals(tableName, tableDescription.getTableName());

        assertThatTableKeySchemaContainsBothKeys(tableDescription.getKeySchema());

        assertEquals("ACTIVE", tableDescription.getTableStatus());
        assertThat(tableDescription.getTableArn(), containsString(tableName));

        ListTablesResult tables = localDynamo.listTables();
        assertEquals(SINGLE_TABLE_EXPECTED, tables.getTableNames().size());
        return localDynamo;
    }

    /**
     * Closes db.
     */
    @AfterEach
    public void closeDB() {
        if (nonNull(localDynamo)) {
            localDynamo.shutdown();
        }
    }

    private void assertThatTableKeySchemaContainsBothKeys(List<KeySchemaElement> tableKeySchema) {
        assertThat(tableKeySchema.toString(), containsString(HASH_KEY_NAME));
        assertThat(tableKeySchema.toString(), containsString(SORT_KEY_NAME));
    }

    private AmazonDynamoDB createLocalDynamoDbMock() {
        return DynamoDBEmbedded.create().amazonDynamoDB();
    }

    private String readTablenNameFronEnvironment() {
        return envWithTableName.readEnv(DatabaseService.USERS_AND_ROLES_TABLE_NAME_ENV_VARIABLE);
    }

    private static CreateTableResult createTable(AmazonDynamoDB ddb, String tableName) {
        List<AttributeDefinition> attributeDefinitions = defineKeyAttributes();
        List<KeySchemaElement> keySchema = defineKeySchema();
        ProvisionedThroughput provisionedthroughput = provisionedThroughputForLocalDatabase();

        CreateTableRequest request =
            new CreateTableRequest()
                .withTableName(tableName)
                .withAttributeDefinitions(attributeDefinitions)
                .withKeySchema(keySchema)
                .withProvisionedThroughput(provisionedthroughput)
                .withGlobalSecondaryIndexes(searchByInstitutionSecondaryIndex());

        return ddb.createTable(request);
    }

    private static GlobalSecondaryIndex searchByInstitutionSecondaryIndex() {
        ProvisionedThroughput provisionedthroughput = provisionedThroughputForLocalDatabase();

        return new GlobalSecondaryIndex()
            .withIndexName(SEARCH_USERS_BY_INSTITUTION_INDEX_NAME)
            .withKeySchema(
                new KeySchemaElement(SECONDARY_INDEX_1_HASH_KEY, KeyType.HASH),
                new KeySchemaElement(SECONDARY_INDEX_1_RANGE_KEY, KeyType.RANGE)
            )
            .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
            .withProvisionedThroughput(provisionedthroughput);
    }

    private static List<KeySchemaElement> defineKeySchema() {
        List<KeySchemaElement> keySchemaElements = new ArrayList<>();
        keySchemaElements.add(new KeySchemaElement(HASH_KEY_NAME, KeyType.HASH));
        keySchemaElements.add(new KeySchemaElement(SORT_KEY_NAME, KeyType.RANGE));
        return keySchemaElements;
    }

    private static List<AttributeDefinition> defineKeyAttributes() {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition(HASH_KEY_NAME, ScalarAttributeType.S));
        attributeDefinitions.add(new AttributeDefinition(SORT_KEY_NAME, ScalarAttributeType.S));
        attributeDefinitions.add(new AttributeDefinition(SECONDARY_INDEX_1_HASH_KEY, ScalarAttributeType.S));
        attributeDefinitions.add(new AttributeDefinition(SECONDARY_INDEX_1_RANGE_KEY, ScalarAttributeType.S));
        return attributeDefinitions;
    }

    private static ProvisionedThroughput provisionedThroughputForLocalDatabase() {
        // not sure if provisioned throughput plays any role in Local databases.
        return new ProvisionedThroughput(CAPACITY_DOES_NOT_MATTER, CAPACITY_DOES_NOT_MATTER);
    }
}

package no.unit.nva.database;

import static java.util.Objects.nonNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
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
    public static final String HASH_KEY_NAME = "PK1A";
    public static final String SORT_KEY_NAME = "PK1B";
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
        String tableName = readTableNableFromEnv();
        CreateTableResult res = createTable(localDynamo, tableName);
        TableDescription tableDesc = res.getTableDescription();
        assertEquals(tableName, tableDesc.getTableName());
        assertThat(tableDesc.getKeySchema().toString(), containsString(HASH_KEY_NAME));
        assertThat(tableDesc.getKeySchema().toString(), containsString(SORT_KEY_NAME));

        assertEquals("ACTIVE", tableDesc.getTableStatus());
        assertThat(tableDesc.getTableArn(), containsString(tableName));
        ListTablesResult tables = localDynamo.listTables();
        assertEquals(1, tables.getTableNames().size());
        return localDynamo;
    }

    private AmazonDynamoDB createLocalDynamoDbMock() {
        return DynamoDBEmbedded.create().amazonDynamoDB();
    }

    private String readTableNableFromEnv() {
        return envWithTableName.readEnv(DatabaseService.USERS_AND_ROLES_TABLE_NAME_ENV_VARIABLE);
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

    private static CreateTableResult createTable(AmazonDynamoDB ddb, String tableName) {
        List<AttributeDefinition> attributeDefinitions = defineKeyAttributes();
        List<KeySchemaElement> keySchema = defineKeySchema();
        ProvisionedThroughput provisionedthroughput = provisionedThroughputForLocalDatabase();

        CreateTableRequest request =
            new CreateTableRequest()
                .withTableName(tableName)
                .withAttributeDefinitions(attributeDefinitions)
                .withKeySchema(keySchema)
                .withProvisionedThroughput(provisionedthroughput);

        return ddb.createTable(request);
    }

    private static List<KeySchemaElement> defineKeySchema() {
        List<KeySchemaElement> ks = new ArrayList<>();
        ks.add(new KeySchemaElement(HASH_KEY_NAME, KeyType.HASH));
        ks.add(new KeySchemaElement(SORT_KEY_NAME, KeyType.RANGE));
        return ks;
    }

    private static List<AttributeDefinition> defineKeyAttributes() {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition(HASH_KEY_NAME, ScalarAttributeType.S));
        attributeDefinitions.add(new AttributeDefinition(SORT_KEY_NAME, ScalarAttributeType.S));
        return attributeDefinitions;
    }

    private static ProvisionedThroughput provisionedThroughputForLocalDatabase() {
        // not sure if provisioned throughput plays any role in Local databases.
        return new ProvisionedThroughput(CAPACITY_DOES_NOT_MATTER, CAPACITY_DOES_NOT_MATTER);
    }
}

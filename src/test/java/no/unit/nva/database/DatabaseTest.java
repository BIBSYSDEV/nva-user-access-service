package no.unit.nva.database;

import static java.util.Objects.nonNull;
import static no.unit.nva.database.DatabaseService.TABLE_NAME;
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
import org.junit.jupiter.api.AfterEach;

public abstract class DatabaseTest {

    protected AmazonDynamoDB localDynamo;

    public final AmazonDynamoDB initializeTestDatabase() {

        localDynamo = DynamoDBEmbedded.create().amazonDynamoDB();

        String hashKeyName = "PK1A";
        String sortKeyName = "PK1B";
        CreateTableResult res = createTable(localDynamo, TABLE_NAME, hashKeyName, sortKeyName);
        TableDescription tableDesc = res.getTableDescription();
        assertEquals(TABLE_NAME, tableDesc.getTableName());
        assertThat(tableDesc.getKeySchema().toString(), containsString(hashKeyName));
        assertThat(tableDesc.getKeySchema().toString(), containsString(sortKeyName));

        assertEquals("ACTIVE", tableDesc.getTableStatus());
        assertThat(tableDesc.getTableArn(), containsString(TABLE_NAME));
        ListTablesResult tables = localDynamo.listTables();
        assertEquals(1, tables.getTableNames().size());
        return localDynamo;
    }

    @AfterEach
    public void closeDB() {
        if (nonNull(localDynamo)) {
            localDynamo.shutdown();
        }
    }

    private static CreateTableResult createTable(AmazonDynamoDB ddb, String tableName, String hashKeyName,
                                                 String sortKeyName) {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition(hashKeyName, ScalarAttributeType.S));
        attributeDefinitions.add(new AttributeDefinition(sortKeyName, ScalarAttributeType.S));

        List<KeySchemaElement> ks = new ArrayList<KeySchemaElement>();
        ks.add(new KeySchemaElement(hashKeyName, KeyType.HASH));
        ks.add(new KeySchemaElement(sortKeyName, KeyType.RANGE));

        ProvisionedThroughput provisionedthroughput = new ProvisionedThroughput(1000L, 1000L);

        CreateTableRequest request =
            new CreateTableRequest()
                .withTableName(tableName)
                .withAttributeDefinitions(attributeDefinitions)
                .withKeySchema(ks)
                .withProvisionedThroughput(provisionedthroughput);

        return ddb.createTable(request);
    }
}

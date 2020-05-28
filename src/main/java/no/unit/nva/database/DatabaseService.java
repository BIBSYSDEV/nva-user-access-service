package no.unit.nva.database;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import java.util.List;
import no.unit.nva.model.UserDto;
import nva.commons.utils.SingletonCollector;

public class DatabaseService {

    private AmazonDynamoDB client;
    private DynamoDBMapper mapper;

    public DatabaseService() {
        client = AmazonDynamoDBClientBuilder.defaultClient();
        mapper = new DynamoDBMapper(client);
    }

    public DatabaseService(AmazonDynamoDB localDynamo) {
        this.client=localDynamo;
        mapper= new DynamoDBMapper(client);
    }

    public void insertUser(UserDb user){
        mapper.save(user);
    }

    public UserDto getUser(String id){

        UserDb userId = new UserDb();
        userId.setId(id);
        Condition comparisonCondition= new Condition();
        comparisonCondition.setComparisonOperator(ComparisonOperator.EQ);
        comparisonCondition.setAttributeValueList(List.of(new AttributeValue(UserDb.TYPE)));
        DynamoDBQueryExpression<UserDb> query= new DynamoDBQueryExpression<UserDb>()
            .withHashKeyValues(userId).withRangeKeyCondition("Type",comparisonCondition );

        List<UserDb> result = mapper.query(UserDb.class, query);
        return result.stream().map(UserDto::fromUserDb).collect(SingletonCollector.collect());
    }

    public void addUser(UserDto user) {
        mapper.save(user.toUserDb());
    }
}

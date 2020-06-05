package no.unit.nva.database;

import static nva.commons.utils.attempt.Try.attempt;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import java.util.List;
import java.util.Optional;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import no.unit.nva.database.exceptions.InvalidUserException;
import no.unit.nva.model.UserDto;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.SingletonCollector;
import nva.commons.utils.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseServiceImpl implements DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseServiceImpl.class);
    public static final String RANGE_KEY_NAME = "PK1B";
    public static final String INVALID_USER_IN_DATABASE = "Invalid user stored in the database:";
    public static final String MISSING_USERNAME = "missing username";
    private AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    @JacocoGenerated
    public DatabaseServiceImpl() {
        client = AmazonDynamoDBClientBuilder.defaultClient();
        mapper = new DynamoDBMapper(client);
    }

    public DatabaseServiceImpl(AmazonDynamoDB localDynamo) {
        this.client = localDynamo;
        mapper = new DynamoDBMapper(client);
    }

    public DatabaseServiceImpl(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<UserDto> getUser(String username) throws InvalidUserException {
        UserDb searchObject = UserDto.newBuilder().withUsername(username).build().toUserDb();
        DynamoDBQueryExpression<UserDb> query = createGetUserQuery(searchObject);
        List<UserDb> result = mapper.query(UserDb.class, query);

        UserDto user = result
            .stream()
            .map(attempt(UserDto::fromUserDb))
            .map(effort -> effort.orElseThrow(this::unexpectedException))
            .collect(SingletonCollector.collectOrElse(null));

        return Optional.ofNullable(user);
    }

    private DynamoDBQueryExpression<UserDb> createGetUserQuery(UserDb searchObject) {
        Condition comparisonCondition = entityTypeAsRangeKey();
        return new DynamoDBQueryExpression<UserDb>()
            .withHashKeyValues(searchObject)
            .withRangeKeyCondition(RANGE_KEY_NAME, comparisonCondition);
    }

    private IllegalStateException unexpectedException(Failure<UserDto> failure) {
        logger.error(INVALID_USER_IN_DATABASE + MISSING_USERNAME);
        throw new IllegalStateException(INVALID_USER_IN_DATABASE, failure.getException());
    }

    private Condition entityTypeAsRangeKey() {
        Condition comparisonCondition = new Condition();
        comparisonCondition.setComparisonOperator(ComparisonOperator.EQ);
        comparisonCondition.setAttributeValueList(List.of(new AttributeValue(UserDb.TYPE)));
        return comparisonCondition;
    }

    @Override
    public void addUser(UserDto user) throws InvalidUserException {
        mapper.save(user.toUserDb());
    }

    @Override
    public UserDto updateUser(UserDto user) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

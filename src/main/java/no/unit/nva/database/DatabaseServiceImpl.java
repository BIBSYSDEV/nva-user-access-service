package no.unit.nva.database;

import static nva.commons.utils.attempt.Try.attempt;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import java.util.List;
import java.util.Optional;
import no.unit.nva.database.exceptions.InvalidInputRoleException;
import no.unit.nva.database.exceptions.InvalidRoleException;
import no.unit.nva.database.exceptions.InvalidUserException;
import no.unit.nva.database.intefaces.WithType;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.SingletonCollector;
import nva.commons.utils.attempt.Failure;
import nva.commons.utils.attempt.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseServiceImpl implements DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseServiceImpl.class);
    public static final String RANGE_KEY_NAME = "PK1B";
    public static final String INVALID_USER_IN_DATABASE = "Invalid user stored in the database:";
    public static final String MISSING_USERNAME = "missing username";
    public static final String INVALID_ROLE_ERROR = "InvalidRole, null object or missing data.";

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
        DynamoDBQueryExpression<UserDb> searchUserByUsername = createGetQuery(searchObject);
        List<UserDb> userSearchResult = mapper.query(UserDb.class, searchUserByUsername);

        UserDto user = userSearchResult
            .stream()
            .map(attempt(UserDto::fromUserDb))
            .map(attempt -> attempt.orElseThrow(this::unexpectedException))
            .collect(SingletonCollector.collectOrElse(null));

        return Optional.ofNullable(user);
    }

    @Override
    public void addUser(UserDto user) throws InvalidUserException {
        mapper.save(user.toUserDb());
    }

    @Override
    public void addRole(RoleDto roleDto) throws InvalidInputRoleException {
        Try.of(roleDto)
            .forEach(role -> mapper.save(roleDto.toRoleDb()))
            .orElseThrow(failure -> new InvalidInputRoleException(INVALID_ROLE_ERROR));
    }

    @Override
    public UserDto updateUser(UserDto user) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<RoleDto> getRole(RoleDto input) throws InvalidRoleException {
        RoleDb searchObject = input.toRoleDb();
        DynamoDBQueryExpression<RoleDb> searchRoleByName = createGetQuery(searchObject);

        PaginatedQueryList<RoleDb> searchRoleByNameResult = mapper.query(RoleDb.class, searchRoleByName);
        RoleDto retrievedRole =
            searchRoleByNameResult
                .stream()
                .map(attempt(RoleDto::fromRoleDb))
                .map(attempt -> attempt.orElseThrow(this::unexpectedException))
                .collect(SingletonCollector.collectOrElse(null));
        return Optional.ofNullable(retrievedRole);
    }

    private static <I extends WithType> DynamoDBQueryExpression<I> createGetQuery(I searchObject) {
        return new DynamoDBQueryExpression<I>()
            .withHashKeyValues(searchObject)
            .withRangeKeyCondition(RANGE_KEY_NAME, entityTypeAsRangeKey(searchObject));
    }

    private <I> IllegalStateException unexpectedException(Failure<I> failure) {
        logger.error(INVALID_USER_IN_DATABASE + MISSING_USERNAME);
        throw new IllegalStateException(INVALID_USER_IN_DATABASE, failure.getException());
    }

    private static <I extends WithType> Condition entityTypeAsRangeKey(I searchObject) {
        Condition comparisonCondition = new Condition();
        comparisonCondition.setComparisonOperator(ComparisonOperator.EQ);
        comparisonCondition.setAttributeValueList(List.of(new AttributeValue(searchObject.getType())));
        return comparisonCondition;
    }
}

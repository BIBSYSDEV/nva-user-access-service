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
import no.unit.nva.database.intefaces.WithType;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidInputRoleException;
import no.unit.nva.exceptions.InvalidRoleInternalException;
import no.unit.nva.exceptions.InvalidUserInternalException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.SingletonCollector;
import nva.commons.utils.attempt.Failure;
import nva.commons.utils.attempt.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseServiceImpl implements DatabaseService {

    public static final String RANGE_KEY_NAME = "PK1B";
    public static final String INVALID_USER_IN_DATABASE = "Invalid user stored in the database:";
    public static final String INVALID_ROLE_ERROR = "InvalidRole, null object or missing data.";
    public static final String USER_ALREAD_EXISTS_ERROR_MESSAGE = "User already exists:";

    private static final Logger logger = LoggerFactory.getLogger(DatabaseServiceImpl.class);
    private final DynamoDBMapper mapper;
    private AmazonDynamoDB client;

    /**
     * Default constructor.
     */
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
    public Optional<UserDto> getUser(UserDto queryObject) throws InvalidUserInternalException {
        logger.debug("Getting user:" + queryObject.toString());
        DynamoDBQueryExpression<UserDb> searchUserRequest = createGetQuery(queryObject.toUserDb());
        List<UserDb> userSearchResult = mapper.query(UserDb.class, searchUserRequest);

        UserDto user = userSearchResult
            .stream()
            .map(attempt(UserDto::fromUserDb))
            .map(attempt -> attempt.orElseThrow(this::unexpectedException))
            .collect(SingletonCollector.collectOrElse(null));

        return Optional.ofNullable(user);
    }

    @Override
    public void addUser(UserDto user) throws InvalidUserInternalException, ConflictException {
        logger.debug("Adding user:" + user.toString());

        if (userAlreadyExists(user)) {
            throw new ConflictException(USER_ALREAD_EXISTS_ERROR_MESSAGE + user.getUsername());
        }
        mapper.save(user.toUserDb());
    }

    private boolean userAlreadyExists(UserDto user) throws InvalidUserInternalException {
        return this.getUser(user).isPresent();
    }

    @Override
    public void addRole(RoleDto roleDto) throws InvalidInputRoleException {
        logger.debug("Adding role:" + roleDto.toString());
        Try.of(roleDto)
            .forEach(role -> mapper.save(roleDto.toRoleDb()))
            .orElseThrow(failure -> new InvalidInputRoleException(INVALID_ROLE_ERROR));
    }

    @Override
    public UserDto updateUser(UserDto user) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<RoleDto> getRole(RoleDto queryObject) throws InvalidRoleInternalException {
        logger.debug("Getting role:" + queryObject.toString());
        RoleDb searchObject = queryObject.toRoleDb();
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

    private static <I extends WithType> Condition entityTypeAsRangeKey(I searchObject) {
        Condition comparisonCondition = new Condition();
        comparisonCondition.setComparisonOperator(ComparisonOperator.EQ);
        comparisonCondition.setAttributeValueList(List.of(new AttributeValue(searchObject.getType())));
        return comparisonCondition;
    }

    private <I> IllegalStateException unexpectedException(Failure<I> failure) {
        throw new IllegalStateException(INVALID_USER_IN_DATABASE, failure.getException());
    }
}

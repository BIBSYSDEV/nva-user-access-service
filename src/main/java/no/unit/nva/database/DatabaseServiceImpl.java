package no.unit.nva.database;

import static java.util.Objects.isNull;
import static nva.commons.utils.JsonUtils.objectMapper;
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
import no.unit.nva.exceptions.EmptyInputException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.JsonSerializable;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.model.Validable;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.SingletonCollector;
import nva.commons.utils.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseServiceImpl extends DatabaseServiceWithTableNameOverride {

    public static final String RANGE_KEY_NAME = "PK1B";
    public static final String EMPTY_INPUT_ERROR_MESSAGE = "Expected non-empty input, but input is empty";
    public static final String INVALID_ENTRY_IN_DATABASE_ERROR = "Invalid entry stored in the database:";
    public static final String USER_ALREADY_EXISTS_ERROR_MESSAGE = "User already exists: ";
    public static final String ROLE_ALREADY_EXISTS_ERROR_MESSAGE = "Role already exists: ";
    public static final String USER_NOT_FOUND_MESSAGE = "Could not find user with username: ";
    public static final String ROLE_NOT_FOUND_MESSAGE = "Could not find role: ";

    public static final String GET_USER_DEBUG_MESSAGE = "Getting user:";
    public static final String GET_ROLE_DEBUG_MESSAGE = "Getting role:";
    public static final String ADD_USER_DEBUG_MESSAGE = "Adding user:";
    public static final String ADD_ROLE_DEBUG_MESSAGE = "Adding role:";
    private static final Logger logger = LoggerFactory.getLogger(DatabaseServiceImpl.class);
    private static final String UPDATE_ROLE_DEBUG_MESSAGE = "Updating role: ";
    private final DynamoDBMapper mapper;

    @JacocoGenerated
    public DatabaseServiceImpl() {
        this(AmazonDynamoDBClientBuilder.defaultClient(), new Environment());
    }

    public DatabaseServiceImpl(AmazonDynamoDB dynamoDbClient, Environment environment) {
        this(createMapperOverridingHardCodedTableName(dynamoDbClient, environment));
    }

    public DatabaseServiceImpl(DynamoDBMapper mapper) {
        super();
        this.mapper = mapper;
    }

    @Override
    public UserDto getUser(UserDto queryObject) throws InvalidEntryInternalException, NotFoundException {
        return getUserAsOptional(queryObject)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE + queryObject.getUsername()));
    }

    @Override
    public void addUser(UserDto user) throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        logger.debug(ADD_USER_DEBUG_MESSAGE + convertToStringOrWriteErrorMessage(user));

        validate(user);
        checkUserDoesNotAlreadyExist(user);
        mapper.save(user.toUserDb());
    }

    @Override
    public void addRole(RoleDto roleDto) throws ConflictException, InvalidInputException,
                                                InvalidEntryInternalException {

        logger.debug(ADD_ROLE_DEBUG_MESSAGE + convertToStringOrWriteErrorMessage(roleDto));

        validate(roleDto);
        checkRoleDoesNotExist(roleDto);
        mapper.save(roleDto.toRoleDb());
    }

    @Override
    public void updateUser(UserDto queryObject)
        throws InvalidEntryInternalException, NotFoundException, InvalidInputException {

        logger.debug(UPDATE_ROLE_DEBUG_MESSAGE + queryObject.toJsonString(objectMapper));

        validate(queryObject);
        UserDto existingUser = getExistingUserOrSendNotFoundError(queryObject);

        if (!existingUser.equals(queryObject)) {
            mapper.save(queryObject.toUserDb());
        }
    }

    @Override
    public RoleDto getRole(RoleDto queryObject) throws NotFoundException, InvalidEntryInternalException {
        return getRoleAsOptional(queryObject)
            .orElseThrow(() -> new NotFoundException(ROLE_NOT_FOUND_MESSAGE + queryObject.getRoleName()));
    }

    @Override
    public Optional<RoleDto> getRoleAsOptional(RoleDto queryObject) throws InvalidEntryInternalException {
        logger.debug(GET_ROLE_DEBUG_MESSAGE + convertToStringOrWriteErrorMessage(queryObject));
        RoleDb searchObject = queryObject.toRoleDb();
        DynamoDBQueryExpression<RoleDb> searchRoleByName = createGetQuery(searchObject);

        PaginatedQueryList<RoleDb> searchRoleByNameResult = mapper.query(RoleDb.class, searchRoleByName);
        return convertQueryResultToOptionalRole(queryObject, searchRoleByNameResult);
    }

    @Override
    public Optional<UserDto> getUserAsOptional(UserDto queryObject) throws InvalidEntryInternalException {
        logger.debug(
            GET_USER_DEBUG_MESSAGE + convertToStringOrWriteErrorMessage(queryObject));
        DynamoDBQueryExpression<UserDb> searchUserRequest = createGetQuery(queryObject.toUserDb());
        List<UserDb> userSearchResult = mapper.query(UserDb.class, searchUserRequest);
        return convertQueryResultToOptionalUser(userSearchResult, queryObject);
    }

    private void checkUserDoesNotAlreadyExist(UserDto user) throws InvalidEntryInternalException, ConflictException {
        if (userAlreadyExists(user)) {
            throw new ConflictException(USER_ALREADY_EXISTS_ERROR_MESSAGE + user.getUsername());
        }
    }

    private void validate(Validable input) throws InvalidInputException {
        if (isNull(input)) {
            throw new EmptyInputException(EMPTY_INPUT_ERROR_MESSAGE);
        }
        if (isInvalid(input)) {
            throw input.exceptionWhenInvalid();
        }
    }

    private boolean isInvalid(Validable roleDto) {
        return isNull(roleDto) || !roleDto.isValid();
    }

    private UserDto getExistingUserOrSendNotFoundError(UserDto queryObject)
        throws NotFoundException, InvalidEntryInternalException {
        return getUserAsOptional(queryObject)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE + queryObject.getUsername()));
    }

    private Optional<RoleDto> convertQueryResultToOptionalRole(RoleDto queryObject,
                                                               List<RoleDb> searchRoleByNameResult) {
        return searchRoleByNameResult
            .stream()
            .map(attempt(RoleDto::fromRoleDb))
            .map(attempt -> attempt.orElseThrow(this::unexpectedException))
            .collect(SingletonCollector.tryCollect())
            .toOptional(failure -> logger.debug(ROLE_NOT_FOUND_MESSAGE + queryObject.getRoleName()));
    }

    private Optional<UserDto> convertQueryResultToOptionalUser(List<UserDb> userSearchResult, UserDto queryObject) {
        return userSearchResult
            .stream()
            .map(attempt(UserDto::fromUserDb))
            .map(attempt -> attempt.orElseThrow(this::unexpectedException))
            .collect(SingletonCollector.tryCollect())
            .toOptional(failure -> logger.debug(USER_NOT_FOUND_MESSAGE + queryObject.getUsername()));
    }

    private void checkRoleDoesNotExist(RoleDto roleDto) throws ConflictException, InvalidEntryInternalException {
        if (roleAlreadyExists(roleDto)) {
            throw new ConflictException(ROLE_ALREADY_EXISTS_ERROR_MESSAGE + roleDto.getRoleName());
        }
    }

    private boolean roleAlreadyExists(RoleDto roleDto) throws InvalidEntryInternalException {
        return getRoleAsOptional(roleDto).isPresent();
    }

    private boolean userAlreadyExists(UserDto user) throws InvalidEntryInternalException {
        return this.getUserAsOptional(user).isPresent();
    }

    private static String convertToStringOrWriteErrorMessage(JsonSerializable queryObject) {
        return Optional.ofNullable(queryObject).map(JsonSerializable::toString).orElse(EMPTY_INPUT_ERROR_MESSAGE);
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
        throw new IllegalStateException(INVALID_ENTRY_IN_DATABASE_ERROR, failure.getException());
    }
}

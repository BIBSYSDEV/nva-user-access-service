package no.unit.nva.database;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_HASH_KEY;
import static no.unit.nva.database.DatabaseIndexDetails.PRIMARY_KEY_RANGE_KEY;
import static no.unit.nva.database.DatabaseIndexDetails.SEARCH_USERS_BY_INSTITUTION_INDEX_NAME;
import static no.unit.nva.database.DatabaseIndexDetails.SECONDARY_INDEX_1_HASH_KEY;
import static nva.commons.utils.JsonUtils.objectMapper;
import static nva.commons.utils.attempt.Try.attempt;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.unit.nva.database.interfaces.DynamoEntryWithRangeKey;
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
import nva.commons.utils.attempt.Failure;
import nva.commons.utils.attempt.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseServiceImpl implements DatabaseService {

    public static final String DYNAMO_DB_CLIENT_NOT_SET_ERROR = "DynamoDb client has not been set";

    public static final String EMPTY_INPUT_ERROR_MESSAGE = "Expected non-empty input, but input is empty";
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
    private final Table table;
    private final Index institutionsIndex;

    @JacocoGenerated
    public DatabaseServiceImpl() {
        this(AmazonDynamoDBClientBuilder.defaultClient(), new Environment());
    }

    public DatabaseServiceImpl(AmazonDynamoDB dynamoDbClient, Environment environment) {
        this(createTable(dynamoDbClient, environment));
    }

    public DatabaseServiceImpl(Table table) {
        super();
        this.table = table;
        this.institutionsIndex = table.getIndex(SEARCH_USERS_BY_INSTITUTION_INDEX_NAME);
    }

    @Override
    public UserDto getUser(UserDto queryObject) throws InvalidEntryInternalException, NotFoundException {
        return getUserAsOptional(queryObject)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE + queryObject.getUsername()));
    }

    @Override
    public List<UserDto> listUsers(String institutionId) {
        QuerySpec listUsersQuery = createListUsersByInstitutionQuery(institutionId);
        List<Item> items = toList(institutionsIndex.query(listUsersQuery));

        return items.stream()
            .map(item -> UserDb.fromItem(item, UserDb.class))
            .map(attempt(UserDto::fromUserDb))
            .flatMap(Try::stream)
            .collect(Collectors.toList());
    }

    @Override
    public void addUser(UserDto user) throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        logger.debug(ADD_USER_DEBUG_MESSAGE + convertToStringOrWriteErrorMessage(user));

        validate(user);
        checkUserDoesNotAlreadyExist(user);
        table.putItem(user.toUserDb().toItem());
    }

    @Override
    public void addRole(RoleDto roleDto) throws ConflictException, InvalidInputException,
                                                InvalidEntryInternalException {

        logger.debug(ADD_ROLE_DEBUG_MESSAGE + convertToStringOrWriteErrorMessage(roleDto));

        validate(roleDto);
        checkRoleDoesNotExist(roleDto);
        table.putItem(roleDto.toRoleDb().toItem());
    }

    @Override
    public void updateUser(UserDto queryObject)
        throws InvalidEntryInternalException, NotFoundException, InvalidInputException {

        logger.debug(UPDATE_ROLE_DEBUG_MESSAGE + queryObject.toJsonString(objectMapper));

        validate(queryObject);
        UserDto existingUser = getExistingUserOrSendNotFoundError(queryObject);

        if (!existingUser.equals(queryObject)) {
            table.putItem(queryObject.toUserDb().toItem());
        }
    }

    @Override
    public RoleDto getRole(RoleDto queryObject) throws NotFoundException, InvalidEntryInternalException {
        return getRoleAsOptional(queryObject)
            .orElseThrow(() -> handleRoleNotFound(queryObject));
    }

    @Override
    public Optional<RoleDto> getRoleAsOptional(RoleDto queryObject) throws InvalidEntryInternalException {
        logger.debug(GET_ROLE_DEBUG_MESSAGE + convertToStringOrWriteErrorMessage(queryObject));
        return Optional.ofNullable(attemptFetchRole(queryObject));
    }

    @Override
    public Optional<UserDto> getUserAsOptional(UserDto queryObject) throws InvalidEntryInternalException {
        logger.debug(GET_USER_DEBUG_MESSAGE + convertToStringOrWriteErrorMessage(queryObject));
        UserDto searchResult = attemptToFetchObject(queryObject);
        return Optional.ofNullable(searchResult);
    }

    protected static Table createTable(AmazonDynamoDB dynamoDbClient, Environment environment) {
        assertDynamoClientIsNotNull(dynamoDbClient);
        String tableName = environment.readEnv(USERS_AND_ROLES_TABLE_NAME_ENV_VARIABLE);
        return new Table(dynamoDbClient, tableName);
    }

    protected static Item fetchItem(Table table, DynamoEntryWithRangeKey requestEntry) {
        return table.getItem(
            PRIMARY_KEY_HASH_KEY, requestEntry.getPrimaryHashKey(),
            PRIMARY_KEY_RANGE_KEY, requestEntry.getPrimaryRangeKey()
        );
    }

    private static void assertDynamoClientIsNotNull(AmazonDynamoDB dynamoDbClient) {
        attempt(() -> requireNonNull(dynamoDbClient))
            .orElseThrow(DatabaseServiceImpl::logErrorWithDynamoClientAndThrowException);
    }

    private static String convertToStringOrWriteErrorMessage(JsonSerializable queryObject) {
        return Optional.ofNullable(queryObject).map(JsonSerializable::toString).orElse(EMPTY_INPUT_ERROR_MESSAGE);
    }

    private static NotFoundException handleRoleNotFound(RoleDto queryObject) {
        logger.debug(ROLE_NOT_FOUND_MESSAGE + queryObject.getRoleName());
        return new NotFoundException(ROLE_NOT_FOUND_MESSAGE + queryObject.getRoleName());
    }

    private static <T> InvalidEntryInternalException handleError(Failure<T> fail) {
        if (fail.getException() instanceof InvalidEntryInternalException) {
            return (InvalidEntryInternalException) fail.getException();
        } else {
            throw new RuntimeException(fail.getException());
        }
    }

    private static void validate(Validable input) throws InvalidInputException {
        if (isNull(input)) {
            throw new EmptyInputException(EMPTY_INPUT_ERROR_MESSAGE);
        }
        if (isInvalid(input)) {
            throw input.exceptionWhenInvalid();
        }
    }

    private static boolean isInvalid(Validable roleDto) {
        return isNull(roleDto) || !roleDto.isValid();
    }

    private static RuntimeException logErrorWithDynamoClientAndThrowException(Failure<AmazonDynamoDB> failure) {
        logger.error(DYNAMO_DB_CLIENT_NOT_SET_ERROR);
        throw new RuntimeException(failure.getException());
    }

    private List<Item> toList(ItemCollection<QueryOutcome> searchResult) {
        List<Item> items = new ArrayList<>();
        for (Item item : searchResult) {
            items.add(item);
        }
        return items;
    }

    private QuerySpec createListUsersByInstitutionQuery(String institution) {
        return new QuerySpec().withHashKey(SECONDARY_INDEX_1_HASH_KEY, institution)
            .withConsistentRead(false);
    }

    private UserDto attemptToFetchObject(UserDto queryObject) throws InvalidEntryInternalException {
        UserDb userDb = attempt(queryObject::toUserDb)
            .map(this::fetchItem)
            .map(item -> UserDb.fromItem(item, UserDb.class))
            .orElseThrow(DatabaseServiceImpl::handleError);
        return nonNull(userDb) ? UserDto.fromUserDb(userDb) : null;
    }

    private Item fetchItem(DynamoEntryWithRangeKey requestEntry) {
        return fetchItem(table, requestEntry);
    }

    private RoleDto attemptFetchRole(RoleDto queryObject) throws InvalidEntryInternalException {
        RoleDb roledb = Try.of(queryObject)
            .map(RoleDto::toRoleDb)
            .map(this::fetchItem)
            .map(item -> RoleDb.fromItem(item, RoleDb.class))
            .orElseThrow(DatabaseServiceImpl::handleError);
        return nonNull(roledb) ? RoleDto.fromRoleDb(roledb) : null;
    }

    private void checkUserDoesNotAlreadyExist(UserDto user) throws InvalidEntryInternalException, ConflictException {
        if (userAlreadyExists(user)) {
            throw new ConflictException(USER_ALREADY_EXISTS_ERROR_MESSAGE + user.getUsername());
        }
    }

    private UserDto getExistingUserOrSendNotFoundError(UserDto queryObject)
        throws NotFoundException, InvalidEntryInternalException {
        return getUserAsOptional(queryObject)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE + queryObject.getUsername()));
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
}

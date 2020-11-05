package no.unit.nva.database;

import static java.util.Objects.nonNull;
import static no.unit.nva.database.DatabaseIndexDetails.SEARCH_USERS_BY_INSTITUTION_INDEX_NAME;
import static no.unit.nva.database.DatabaseIndexDetails.SECONDARY_INDEX_1_HASH_KEY;
import static nva.commons.utils.JsonUtils.objectMapper;
import static nva.commons.utils.attempt.Try.attempt;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.exceptions.NotFoundException;
import no.unit.nva.model.UserDto;
import nva.commons.utils.attempt.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService extends DatabaseSubService {

    public static final String USER_NOT_FOUND_MESSAGE = "Could not find user with username: ";
    public static final String GET_USER_DEBUG_MESSAGE = "Getting user: ";
    public static final String ADD_USER_DEBUG_MESSAGE = "Adding user: ";
    public static final String UPDATE_USER_DEBUG_MESSAGE = "Updating user: ";
    public static final String USER_ALREADY_EXISTS_ERROR_MESSAGE = "User already exists: ";

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final Index institutionsIndex;
    private final RoleService roleService;

    public UserService(Table table, RoleService roleService) {
        super(table);
        this.roleService = roleService;
        this.institutionsIndex = this.table.getIndex(SEARCH_USERS_BY_INSTITUTION_INDEX_NAME);
    }

    /**
     * Fetches a user from the database that has the username specified in the input.
     *
     * @param queryObject the DTO containing the search information.
     * @return the DTO of the user in the database.
     * @throws InvalidEntryInternalException when the entry stored in the database is invalid
     * @throws NotFoundException             when there is no use with that username
     */
    public UserDto getUser(UserDto queryObject) throws InvalidEntryInternalException, NotFoundException {
        return getUserAsOptional(queryObject)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MESSAGE + queryObject.getUsername()));
    }

    /**
     * List of users for a specified institution.
     *
     * @param institutionIdentifier the identifer of the insituttion
     * @return all users of the specified institution.
     */
    public List<UserDto> listUsers(String institutionIdentifier) {
        QuerySpec listUsersQuery = createListUsersByInstitutionQuery(institutionIdentifier);
        List<Item> items = toList(institutionsIndex.query(listUsersQuery));

        return items.stream()
            .map(item -> UserDb.fromItem(item, UserDb.class))
            .map(attempt(UserDto::fromUserDb))
            .flatMap(Try::stream)
            .collect(Collectors.toList());
    }

    /**
     * Adds a user.
     *
     * @param user the user to be added.
     * @throws InvalidEntryInternalException when a user with same username exists and the entry in the database is
     *                                       invalid.
     * @throws ConflictException             when the entry exists.
     * @throws InvalidInputException         when the input entry is not valid.
     */
    public void addUser(UserDto user) throws InvalidEntryInternalException, ConflictException, InvalidInputException {
        logger.debug(ADD_USER_DEBUG_MESSAGE + convertToStringOrWriteErrorMessage(user));

        validate(user);
        checkUserDoesNotAlreadyExist(user);
        table.putItem(user.toUserDb().toItem());
    }

    /**
     * Update an existing user.
     *
     * @param queryObject the updated user information.
     * @throws InvalidEntryInternalException when a user with same username exists and the entry in the database is
     *                                       invalid.
     * @throws InvalidInputException         when the input entry is invalid.
     * @throws NotFoundException             when there is no user with the same username in the database.
     */
    public void updateUser(UserDto queryObject)
        throws InvalidEntryInternalException, InvalidInputException, NotFoundException {

        logger.debug(UPDATE_USER_DEBUG_MESSAGE + queryObject.toJsonString(objectMapper));
        validate(queryObject);
        UserDto existingUser = getExistingUserOrSendNotFoundError(queryObject);
        UserDb desiredUpdate = queryObject.toUserDb();
        UserDb desiredUpdateWithSyncedRoles = userWithSyncedRoles(desiredUpdate);
        if (userHasChanged(existingUser, desiredUpdateWithSyncedRoles)) {
            updateTable(desiredUpdateWithSyncedRoles);
        }
    }

    private Optional<UserDto> getUserAsOptional(UserDto queryObject) throws InvalidEntryInternalException {
        logger.debug(GET_USER_DEBUG_MESSAGE + convertToStringOrWriteErrorMessage(queryObject));
        UserDto searchResult = attemptToFetchObject(queryObject);
        return Optional.ofNullable(searchResult);
    }

    private QuerySpec createListUsersByInstitutionQuery(String institution) {
        return new QuerySpec().withHashKey(SECONDARY_INDEX_1_HASH_KEY, institution)
            .withConsistentRead(false);
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

    private boolean userAlreadyExists(UserDto user) throws InvalidEntryInternalException {
        return this.getUserAsOptional(user).isPresent();
    }

    private List<Item> toList(ItemCollection<QueryOutcome> searchResult) {
        List<Item> items = new ArrayList<>();
        for (Item item : searchResult) {
            items.add(item);
        }
        return items;
    }

    private UserDto attemptToFetchObject(UserDto queryObject) throws InvalidEntryInternalException {
        UserDb userDb = attempt(queryObject::toUserDb)
            .map(this::fetchItem)
            .map(item -> UserDb.fromItem(item, UserDb.class))
            .orElseThrow(DatabaseSubService::handleError);
        return nonNull(userDb) ? UserDto.fromUserDb(userDb) : null;
    }

    private boolean userHasChanged(UserDto existingUser, UserDb desiredUpdateWithSyncedRoles)
        throws InvalidEntryInternalException {
        return !desiredUpdateWithSyncedRoles.equals(existingUser.toUserDb());
    }

    private void updateTable(UserDb userUpdateWithSyncedRoles) {

        table.putItem(userUpdateWithSyncedRoles.toItem());
    }

    private UserDb userWithSyncedRoles(UserDb currentUser) throws InvalidEntryInternalException {
        List<RoleDb> roles = currentRoles(currentUser);
        return currentUser.copy().withRoles(roles).build();
    }

    private List<RoleDb> currentRoles(UserDb currentUser) {
        return currentUser
            .getRoles()
            .stream()
            .map(roleService::fetchRoleDao)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
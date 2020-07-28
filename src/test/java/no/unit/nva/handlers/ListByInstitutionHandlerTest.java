package no.unit.nva.handlers;

import static no.unit.nva.handlers.ListByInstitutionHandler.INSTITUTION_ID_PATH_PARAMETER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import no.unit.nva.exceptions.ConflictException;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.exceptions.InvalidInputException;
import no.unit.nva.model.UserList;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.GatewayResponse;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListByInstitutionHandlerTest extends HandlerTest {

    public static final String SOME_OTHER_USERNAME = "SomeOtherUsername";
    private ListByInstitutionHandler listByInstitutionHandler;
    private Context context;

    @BeforeEach
    public void init() {
        listByInstitutionHandler = new ListByInstitutionHandler(mockEnvironment());
        context = mock(Context.class);
        databaseService = createDatabaseServiceUsingLocalStorage();
    }

    @Test
    public void handleRequestReturnsOkUponSuccessfulRequest() throws IOException {
        InputStream validRequest = createValidRequest();

        ByteArrayOutputStream output = sendRequestToHandler(validRequest);

        GatewayResponse<UserList> response = GatewayResponse.fromOutputStream(output);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.SC_OK)));
    }

    @Test
    public void handleRequestReturnsListOfUsersGivenAnInstitution()
        throws IOException, ConflictException, InvalidEntryInternalException, InvalidInputException {
        UserList expectedUsers = insertTwoUsersOfSameInstitution();

        InputStream validRequest = createValidRequest();
        ByteArrayOutputStream output = sendRequestToHandler(validRequest);

        GatewayResponse<UserList> response = GatewayResponse.fromOutputStream(output);
        UserList actualUsers = response.getBodyObject(UserList.class);
        assertThatListsAreEquivalent(expectedUsers, actualUsers);
    }

    private ByteArrayOutputStream sendRequestToHandler(InputStream validRequest) throws IOException {
        ByteArrayOutputStream output = outputStream();
        listByInstitutionHandler.handleRequest(validRequest, output, context);
        return output;
    }

    private void assertThatListsAreEquivalent(UserList expectedUsers, UserList actualUsers) {
        assertThat(actualUsers, containsInAnyOrder(expectedUsers.toArray()));
        assertThat(expectedUsers, containsInAnyOrder(actualUsers.toArray()));
    }

    private UserList insertTwoUsersOfSameInstitution()
        throws ConflictException, InvalidEntryInternalException, InvalidInputException {
        UserList users = new UserList();
        users.add(insertSampleUserToDatabase(SOME_USERNAME, SOME_INSTITUTION));
        users.add(insertSampleUserToDatabase(SOME_OTHER_USERNAME, SOME_INSTITUTION));
        return users;
    }

    private ByteArrayOutputStream outputStream() {
        return new ByteArrayOutputStream();
    }

    private InputStream createValidRequest() throws JsonProcessingException {
        Map<String, String> pathParams = Map.of(INSTITUTION_ID_PATH_PARAMETER, SOME_INSTITUTION);
        return new HandlerRequestBuilder<Void>(JsonUtils.objectMapper)
            .withPathParameters(pathParams)
            .build();
    }
}
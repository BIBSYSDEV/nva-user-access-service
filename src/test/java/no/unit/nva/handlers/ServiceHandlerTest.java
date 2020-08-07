package no.unit.nva.handlers;

import static no.unit.nva.handlers.HandlerAccessingUser.USERNAME_PATH_PARAMETER;
import static nva.commons.utils.JsonUtils.objectMapper;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import no.unit.nva.exceptions.InvalidEntryInternalException;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.handlers.ApiGatewayHandler;

public class ServiceHandlerTest extends HandlerTest {

    protected final UserDto sampleUser;
    private final Context context;

    protected ServiceHandlerTest() throws InvalidEntryInternalException {
        sampleUser = createSampleUser();
        context = mock(Context.class);
    }

    protected static Map<String, String> map(String key, String value) {
        return Collections.singletonMap(key, value);
    }

    protected <I, O> ByteArrayOutputStream sendRequest(ApiGatewayHandler<I, O> handler,
                                                       I body,
                                                       Map<String, String> headers)
        throws IOException {

        Map<String, String> pathParameters = map(USERNAME_PATH_PARAMETER, sampleUser.getUsername());

        InputStream input = new HandlerRequestBuilder<I>(objectMapper)
            .withBody(body)
            .withPathParameters(pathParameters)
            .withHeaders(headers)
            .build();
        ByteArrayOutputStream output = outputStream();
        handler.handleRequest(input, output, context);
        return output;
    }

    private static UserDto createSampleUser() throws InvalidEntryInternalException {
        RoleDto sampleRole = RoleDto.newBuilder().withName(DEFAULT_ROLE).build();
        return UserDto.newBuilder()
            .withUsername(DEFAULT_USERNAME)
            .withInstitution(DEFAULT_INSTITUTION)
            .withRoles(Collections.singletonList(sampleRole))
            .build();
    }
}
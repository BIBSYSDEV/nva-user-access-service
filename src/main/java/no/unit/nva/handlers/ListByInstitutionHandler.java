package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.model.UserDto;
import no.unit.nva.model.UserList;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class ListByInstitutionHandler extends ApiGatewayHandler<Void, UserList> {

    public static final String INSTITUTION_ID_PATH_PARAMETER = "institutionId";

    @JacocoGenerated
    public ListByInstitutionHandler() {
        this(new Environment());
    }

    public ListByInstitutionHandler(Environment environment) {
        super(Void.class, environment,LoggerFactory.getLogger(ListByInstitutionHandler.class));
    }

    @Override
    protected UserList processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        return new UserList();
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, UserList output) {
        return HttpStatus.SC_OK;
    }
}

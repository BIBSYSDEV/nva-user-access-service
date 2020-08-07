package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;

public class GetUserHandlerForInternalService extends GetUserHandler {

    private final ApiKeyValidation apiKeyValidation;

    @JacocoGenerated
    public GetUserHandlerForInternalService() {
        super();
        this.apiKeyValidation = new ApiKeyValidation(environment);
    }

    public GetUserHandlerForInternalService(Environment environment,
                                            DatabaseService databaseService,
                                            AWSSecretsManager secretsManager
    ) {
        super(environment, databaseService);
        this.apiKeyValidation = new ApiKeyValidation(secretsManager, environment);
    }

    @Override
    protected UserDto processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        apiKeyValidation.authorizeAccess(requestInfo);
        return super.processInput(input, requestInfo, context);
    }
}

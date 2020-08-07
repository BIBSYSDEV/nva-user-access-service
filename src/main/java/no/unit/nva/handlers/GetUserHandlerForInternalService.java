package no.unit.nva.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import no.unit.nva.database.DatabaseService;
import no.unit.nva.model.UserDto;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserHandlerForInternalService extends GetUserHandler {

    private final ApiKeyValidation apiKeyValidation;

    @JacocoGenerated
    public GetUserHandlerForInternalService() {
        super(defaultLogger());
        this.apiKeyValidation = new ApiKeyValidation(environment);
    }

    public GetUserHandlerForInternalService(Environment environment,
                                            DatabaseService databaseService,
                                            AWSSecretsManager secretsManager
    ) {
        super(environment, databaseService, defaultLogger());
        this.apiKeyValidation = new ApiKeyValidation(secretsManager, environment);
    }

    public static Logger defaultLogger() {
        return LoggerFactory.getLogger(GetUserHandlerForInternalService.class);
    }

    @Override
    protected UserDto processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        apiKeyValidation.compareKeys(requestInfo);
        return super.processInput(input, requestInfo, context);
    }
}

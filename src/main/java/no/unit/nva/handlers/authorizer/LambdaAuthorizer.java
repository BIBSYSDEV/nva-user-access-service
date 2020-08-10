package no.unit.nva.handlers.authorizer;

import static nva.commons.utils.attempt.Try.attempt;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Collections;
import nva.commons.exceptions.ApiGatewayException;
import nva.commons.handlers.ApiGatewayHandler;
import nva.commons.handlers.RequestInfo;
import nva.commons.utils.JsonUtils;
import org.apache.http.HttpStatus;
import org.slf4j.LoggerFactory;

public class LambdaAuthorizer extends ApiGatewayHandler<Void, HandlerResponse> {

    public LambdaAuthorizer() {
        super(Void.class, LoggerFactory.getLogger(LambdaAuthorizer.class));
    }

    @Override
    protected HandlerResponse processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        String requestInfoStr = attempt(() -> JsonUtils.objectMapper.writeValueAsString(requestInfo))
            .orElse(fail -> null);
        System.out.println(requestInfoStr);
        logger.info(requestInfoStr);
        //String resource= "arn:aws:execute-api:region:account-id:api-id/stage-name/HTTP-VERB/resource-path-specifier"
        StatementElement statement = StatementElement.newBuilder()
            .withResource("*")
            .withAction("execute-api:Invoke")
            .withEffect("Allow")
            .build();
        AuthPolicy authPolicy = AuthPolicy.newBuilder().withStatement(Collections.singletonList(statement)).build();
        return HandlerResponse.newBuilder().withPrincipalId("SomeUser").withPolicyDocument(authPolicy).build();
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, HandlerResponse output) {
        return HttpStatus.SC_ACCEPTED;
    }
}

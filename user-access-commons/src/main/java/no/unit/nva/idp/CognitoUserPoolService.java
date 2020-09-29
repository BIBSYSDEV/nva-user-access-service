package no.unit.nva.idp;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import java.util.List;
import nva.commons.utils.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CognitoUserPoolService implements UserPoolService {
    private static final Logger logger = LoggerFactory.getLogger(CognitoUserPoolService.class);
    private final AWSCognitoIdentityProvider identityProvider;

    @JacocoGenerated
    public CognitoUserPoolService() {
        this(newCognitoIdentityProviderClient());
    }

    public CognitoUserPoolService(AWSCognitoIdentityProvider awsCognitoIdentityProvider) {
        this.identityProvider = awsCognitoIdentityProvider;
    }

    private static AWSCognitoIdentityProvider newCognitoIdentityProviderClient() {
        return AWSCognitoIdentityProviderClient.builder().build();
    }

    @Override
    public void updateUserAttributes(String idpUserPoolId, String idpUserName, List<AttributeType> attributesToUpdate) {
        AdminUpdateUserAttributesRequest request = new AdminUpdateUserAttributesRequest()
            .withUserPoolId(idpUserPoolId)
            .withUsername(idpUserName)
            .withUserAttributes(attributesToUpdate);
        logger.info("Updating User Attributes: " + request.toString());
        identityProvider.adminUpdateUserAttributes(request);
    }
}

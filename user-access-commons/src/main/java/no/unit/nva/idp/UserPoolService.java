package no.unit.nva.idp;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import java.util.List;

public interface UserPoolService {

    void updateUserAttributes(String idpUserPoolId, String idpUserName, List<AttributeType> attributesToUpdate);
}

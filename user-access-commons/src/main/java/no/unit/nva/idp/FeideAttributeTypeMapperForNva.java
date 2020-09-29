package no.unit.nva.idp;

import com.amazonaws.services.cognitoidp.model.AttributeType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import no.unit.nva.model.RoleDto;
import no.unit.nva.model.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeideAttributeTypeMapperForNva {
    public static final String COMMA_DELIMITER = ",";
    public static final String FEIDE_PREFIX = "feide:";
    private static final Logger logger = LoggerFactory.getLogger(FeideAttributeTypeMapperForNva.class);
    private static final String NVA = "NVA";

    public static List<AttributeType> fromUserDto(UserDto userDto) {
        return getAttributeTypesToUpdate(userDto);
    }

    private static List<AttributeType> getAttributeTypesToUpdate(UserDto userDto) {
        List<AttributeType> userAttributeTypes = new ArrayList<>();

        if (userDto.getInstitution() != null) {
            userAttributeTypes.add(
                toAttributeType(UserAttributes.CUSTOM_CUSTOMER_ID, userDto.getInstitution()));
        }
        if (userDto.getCristinId() != null) {
            userAttributeTypes.add(
                toAttributeType(UserAttributes.CUSTOM_CRISTIN_ID, userDto.getCristinId()));
        }
        userAttributeTypes.add(toAttributeType(UserAttributes.CUSTOM_APPLICATION, NVA));
        userAttributeTypes.add(
            toAttributeType(UserAttributes.CUSTOM_IDENTIFIERS, FEIDE_PREFIX + userDto.getUsername()));

        String applicationRoles = toRolesString(userDto.getRoles());
        logger.info("applicationRoles: " + applicationRoles);
        userAttributeTypes.add(toAttributeType(UserAttributes.CUSTOM_APPLICATION_ROLES, applicationRoles));

        return userAttributeTypes;
    }

    private static AttributeType toAttributeType(String name, String value) {
        AttributeType attributeType = new AttributeType();
        attributeType.setName(name);
        attributeType.setValue(value);
        return attributeType;
    }

    private static String toRolesString(List<RoleDto> roles) {
        return roles
            .stream()
            .map(RoleDto::getRoleName)
            .collect(Collectors.joining(COMMA_DELIMITER));
    }
}

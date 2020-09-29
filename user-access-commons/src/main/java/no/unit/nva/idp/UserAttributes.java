package no.unit.nva.idp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAttributes {

    public static final String CUSTOM_CUSTOMER_ID = "custom:customerId";
    public static final String CUSTOM_CRISTIN_ID = "custom:cristinId";
    public static final String CUSTOM_APPLICATION = "custom:application";

    public static final String CUSTOM_IDENTIFIERS = "custom:identifiers";
    public static final String CUSTOM_APPLICATION_ROLES = "custom:applicationRoles";
    public static final String CUSTOM_FEIDE_ID = "custom:feideId";
    public static final String CUSTOM_ORG_NUMBER = "custom:orgNumber";
    public static final String CUSTOM_AFFILIATION = "custom:affiliation";

    @JsonProperty(CUSTOM_FEIDE_ID)
    private String feideId;

    @JsonProperty(CUSTOM_ORG_NUMBER)
    private String orgNumber;

    @JsonProperty(CUSTOM_AFFILIATION)
    private String affiliation;

    @JsonProperty(CUSTOM_CRISTIN_ID)
    private String cristinId; // populated by our triggers

    @JsonProperty(CUSTOM_CUSTOMER_ID)
    private String customerId; // populated by our triggers

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    public String getFeideId() {
        return feideId;
    }

    public void setFeideId(String feideId) {
        this.feideId = feideId;
    }

    public String getOrgNumber() {
        return orgNumber;
    }

    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getCristinId() {
        return cristinId;
    }

    public void setCristinId(String cristinId) {
        this.cristinId = cristinId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * Returns a deep copy of this instance.
     * @return UserAttributes copy
     */
    @JsonIgnore
    public UserAttributes getDeepCopy() {
        var copy = new UserAttributes();
        copy.setAffiliation(getAffiliation());
        copy.setCristinId(getCristinId());
        copy.setCustomerId(getCustomerId());
        copy.setFamilyName(getFamilyName());
        copy.setFeideId(getFeideId());
        copy.setGivenName(getGivenName());
        copy.setOrgNumber(getOrgNumber());
        return copy;
    }
}

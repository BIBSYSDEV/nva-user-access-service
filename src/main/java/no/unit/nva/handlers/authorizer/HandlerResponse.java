package no.unit.nva.handlers.authorizer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HandlerResponse {

    @JsonProperty("principalId")
    private String principalId;

    @JsonProperty("policyDocument")
    private AuthPolicy policyDocument;

    public HandlerResponse() {
    }

    private HandlerResponse(Builder builder) {
        setPrincipalId(builder.principalId);
        setPolicyDocument(builder.policyDocument);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public AuthPolicy getPolicyDocument() {
        return policyDocument;
    }

    public void setPolicyDocument(AuthPolicy policyDocument) {
        this.policyDocument = policyDocument;
    }

    public static final class Builder {

        private String principalId;
        private AuthPolicy policyDocument;

        private Builder() {
        }

        public Builder withPrincipalId(String val) {
            principalId = val;
            return this;
        }

        public Builder withPolicyDocument(AuthPolicy val) {
            policyDocument = val;
            return this;
        }

        public HandlerResponse build() {
            return new HandlerResponse(this);
        }
    }
}

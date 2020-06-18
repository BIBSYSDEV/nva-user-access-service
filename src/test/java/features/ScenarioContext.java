package features;

import java.util.Map;

public class ScenarioContext {

    public Map<String, Object> getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Map<String, Object> requestBody) {
        this.requestBody = requestBody;
    }

    private Map<String, Object> requestBody;
}

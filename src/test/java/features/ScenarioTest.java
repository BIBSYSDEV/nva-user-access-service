package features;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.datatable.DataTable;
import java.util.Map;
import no.unit.nva.database.intefaces.WithEnvironment;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.utils.JsonUtils;

public abstract class ScenarioTest implements WithEnvironment {


    public static final int IGNORE_HEADER_ROW = 1;


    protected static DataTable ignoreHeadersRow(DataTable datatable){
        return datatable.rows(IGNORE_HEADER_ROW);

    }

    protected static TypeReference<Map<String,Object>> createRequestBuilderTypeRef(){
        return new TypeReference<>() {};
    }

    protected static <T> T readRequestBody(HandlerRequestBuilder<Map<String,Object>> requestBuilder, Class<T> clazz)
        throws JsonProcessingException {
        Map<String, Object> bodyMap = requestBuilder.getBody(createRequestBuilderTypeRef());
        return JsonUtils.objectMapper.convertValue(bodyMap,clazz);

    }

    protected static <T> Map<String,Object>  prepareRequestBody(T requestObject) {
        return JsonUtils.objectMapper.convertValue(requestObject,createRequestBuilderTypeRef());

    }
}

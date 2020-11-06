package no.unit.nva.useraccessmanagement.dao.interfaces;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import nva.commons.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.invocation.InvocationOnMock;

class JsonSerializableTest {

    public static final String EXPECTED_ERROR_MESSAGE = "expectedErrorMessage";

    @Test
    public void JsonSerializableReturnsValidJsonString() throws JsonProcessingException {
        JsonObject object = sampleObject();
        String json = object.toJsonString();
        JsonObject copy = JsonUtils.objectMapper.readValue(json, JsonObject.class);
        assertThat(copy, is(equalTo(object)));
    }

    @Test
    public void toJsonStringThrowsExceptionWithInternalCause() throws JsonProcessingException {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenAnswer(this::throwException);
        JsonObject object = sampleObject();
        Executable action = () -> object.toJsonString(mapper);
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        Throwable cause = exception.getCause();
        assertThat(cause.getMessage(), containsString(EXPECTED_ERROR_MESSAGE));
    }

    public JsonObject sampleObject() {
        JsonObject object = new JsonObject();
        object.setField("SomeValue");
        return object;
    }

    public Object throwException(InvocationOnMock invocation) {
        throw new RuntimeException(EXPECTED_ERROR_MESSAGE);
    }

    private static class JsonObject implements JsonSerializable {

        private String field;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            JsonObject object = (JsonObject) o;
            return Objects.equals(getField(), object.getField());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getField());
        }
    }
}
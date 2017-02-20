package io.openexchange.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openexchange.pojos.api.Response;

import java.io.IOException;
import java.util.Objects;

public abstract class Utils {

    public static <R extends Response> R deserializeFrom(String content, Class<R> rClass) throws IOException, ClassTypeMismatchException {
        content = Objects.requireNonNull(content);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Response baseResponse = mapper.readValue(content, Response.class);
        if (!baseResponse.getClassType().equals(rClass.getCanonicalName())) {
            throw new ClassTypeMismatchException("Content does not match to provided class type");
        }
        return mapper.readValue(content, rClass);
    }

    public static <R extends Response> String serializeTo(R response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.writeValueAsString(response);
    }

    public static class ClassTypeMismatchException extends Exception {
        ClassTypeMismatchException(String message) {
            super(message);
        }
    }
}

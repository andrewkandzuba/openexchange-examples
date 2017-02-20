package io.openexchange.pojos.api;

import io.openexchange.api.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static io.openexchange.api.Utils.deserializeFrom;
import static io.openexchange.api.Utils.serializeTo;

public class ResponseDeserializationTest {
    @Test
    public void successDeserialization() throws Exception {
        RegisterDeviceResponse normalResponse = (RegisterDeviceResponse) new RegisterDeviceResponse()
                .withCode(200)
                .withDescription("OK");
        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse()
                .withCode(500)
                .withDescription("Validation has not passed")
                .withErrors(Collections.singletonList("field1 cannot be null"));

        RegisterDeviceResponse r1 = deserializeFrom(serializeTo(normalResponse), RegisterDeviceResponse.class);
        Assert.assertEquals(200, r1.getCode().intValue());
        Assert.assertEquals("OK", r1.getDescription());

        ValidationErrorResponse r2 = deserializeFrom(serializeTo(validationErrorResponse), ValidationErrorResponse.class);
        Assert.assertEquals(500, r2.getCode().intValue());
        Assert.assertEquals("Validation has not passed", r2.getDescription());
        Assert.assertTrue(r2.getErrors().size() > 0);
        Assert.assertEquals("field1 cannot be null", r2.getErrors().get(0));
    }

    @Test(expected = Utils.ClassTypeMismatchException.class)
    public void failedDeserialization() throws Exception {
        RegisterDeviceResponse normalResponse = (RegisterDeviceResponse) new RegisterDeviceResponse()
                .withCode(200)
                .withDescription("OK");

        String normalResponseContent = serializeTo(normalResponse);
        deserializeFrom(normalResponseContent, Response.class);
        deserializeFrom(normalResponseContent, ValidationErrorResponse.class);
    }
}

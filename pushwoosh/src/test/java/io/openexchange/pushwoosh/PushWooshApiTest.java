package io.openexchange.pushwoosh;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openexchange.pojos.pushwoosh.PushResponse;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class PushWooshApiTest {

    @Test
    public void testPushResponseWithRequestId() throws Exception {
        String payload = "{\"status_code\":200,\"status_message\":\"OK\",\"response\":{\"request_id\":\"40acce40fc44d3ec99db40424e21d1ce\"}}";
        PushResponse pushResponse = new ObjectMapper().readValue(payload, PushResponse.class);
        assertNotNull(pushResponse);
        assertEquals(200, pushResponse.getStatusCode().intValue());
        assertEquals("OK", pushResponse.getStatusMessage());
        assertNotNull(pushResponse.getResponse());
        assertNotNull(pushResponse.getResponse().getMessages());
        assertEquals(0, pushResponse.getResponse().getMessages().size());
        assertNotNull(pushResponse.getResponse().getRequestId());
        assertEquals("40acce40fc44d3ec99db40424e21d1ce", pushResponse.getResponse().getRequestId());
    }

    @Test
    public void testPushResponseWithMessages() throws Exception {
        String payload = "{\"status_code\":200,\"status_message\":\"OK\",\"response\":{\"Messages\":[\"AB76-A0C35DCF-127E7235\"]}}";
        PushResponse pushResponse = new ObjectMapper().readValue(payload, PushResponse.class);
        assertNotNull(pushResponse);
        assertEquals(200, pushResponse.getStatusCode().intValue());
        assertEquals("OK", pushResponse.getStatusMessage());
        assertNotNull(pushResponse.getResponse());
        assertNotNull(pushResponse.getResponse().getMessages());
        assertEquals(1, pushResponse.getResponse().getMessages().size());
        assertEquals("AB76-A0C35DCF-127E7235", pushResponse.getResponse().getMessages().get(0));
        assertNull(pushResponse.getResponse().getRequestId());
    }

    @Test
    public void testPushResponseWithStatistics() throws Exception {
        String payload = "{\"status_code\":200,\"status_message\":\"OK\",\"response\":{\"formatter\":\"minutely\",\"rows\":[{\"datetime\":\"2017-02-17 08:29:00\",\"action\":\"send\",\"count\":\"1\"},{\"datetime\":\"2017-02-17 08:29:00\",\"action\":\"open\",\"count\":0},{\"datetime\":\"2017-02-17 08:30:00\",\"action\":\"open\",\"count\":0},{\"datetime\":\"2017-02-17 08:30:00\",\"action\":\"send\",\"count\":0}]}}";
        PushResponse pushResponse = new ObjectMapper().readValue(payload, PushResponse.class);
        assertNotNull(pushResponse);
        assertEquals(200, pushResponse.getStatusCode().intValue());
        assertEquals("OK", pushResponse.getStatusMessage());
        assertNotNull(pushResponse.getResponse());
        assertNotNull(pushResponse.getResponse().getFormatter());
        assertEquals("minutely", pushResponse.getResponse().getFormatter());
        assertNotNull(pushResponse.getResponse().getRows());
        assertEquals(4, pushResponse.getResponse().getRows().size());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals("2017-02-17 08:29:00", sdf.format(pushResponse.getResponse().getRows().get(0).getDatetime()));
        assertEquals("send", pushResponse.getResponse().getRows().get(0).getAction());
        assertEquals(1, pushResponse.getResponse().getRows().get(0).getCount().intValue());
    }
}

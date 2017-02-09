package io.openexchange.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openexchange.controlllers.SmsController;
import io.openexchange.pojos.Sms;
import io.openexchange.producers.SmsProducer;
import io.openexchange.utils.SmsFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({SmsController.class})
@TestPropertySource(locations = "classpath:test.properties")
public class SmsControllerTest {
    @MockBean
    private SmsProducer smsProducer;
    @InjectMocks
    private SmsController smsController;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        initMocks(this);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(smsController)
                .build();
        Mockito.when(smsProducer.send(Mockito.any(Sms.class))).thenReturn(true);
    }

    @Test
    public void testValidationFailed() throws Exception {
        mockMvc.perform(put("/sms")
                .content(
                        mapper.writeValueAsString(
                                SmsFactory.of(null, null, null)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testValidationSuccess() throws Exception {
        mockMvc.perform(put("/sms")
                .content(
                        mapper.writeValueAsString(
                                SmsFactory.of("1111", "2222", "Hello")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

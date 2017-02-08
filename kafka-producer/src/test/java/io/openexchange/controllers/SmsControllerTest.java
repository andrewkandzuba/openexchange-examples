package io.openexchange.controllers;

import io.openexchange.controlllers.SmsController;
import io.openexchange.pojos.Sms;
import io.openexchange.utils.SmsFactory;
import io.openexchange.producers.SmsProducer;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@RunWith(SpringRunner.class)
@WebMvcTest(SmsController.class)
@TestPropertySource(locations = "classpath:test.properties")
public class SmsControllerTest {
    @MockBean
    SmsProducer smsProducer;
    @InjectMocks
    SmsController smsController;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        initMocks(this);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(smsController)
                .build();
        Mockito.stub(smsProducer.send(Mockito.any(Sms.class))).toReturn(true);
    }

    @Test
    public void testValidation() throws Exception {
        mockMvc.perform(put("/sms", SmsFactory.of(null, null, null))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}

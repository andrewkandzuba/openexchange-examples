package io.openexchange.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openexchange.api.Utils;
import io.openexchange.controlllers.SenderController;
import io.openexchange.pojos.api.*;
import io.openexchange.pojos.domain.Application;
import io.openexchange.pojos.domain.Device;
import io.openexchange.pojos.domain.User;
import io.openexchange.services.PushReply;
import io.openexchange.services.Sender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:test.properties")
public class SenderControllerTest {
    @MockBean
    private Sender sender;
    @InjectMocks
    private SenderController senderController;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private final static String senderPath = "/sender/push/device";
    private final Application application = new Application().withCode("2222-3333");
    private final Device device = new Device().withHwid("xxxxxxx").withToken("yyyyyy").withType(Device.Type._1);
    private final User user = new User().withId("zzzzzz");

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        this.mockMvc = standaloneSetup(senderController).build();
    }

    @Test
    public void validationFailed() throws Exception {
        mockMvc.perform(post(senderPath)
                .content(
                        mapper.writeValueAsString(
                                new CreateMessageRequest()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(mvcResult -> {
                    ValidationErrorResponse r = Utils.deserializeFrom(
                            mvcResult
                                    .getResponse()
                                    .getContentAsString(),
                            ValidationErrorResponse.class);
                    assertEquals(201, r.getCode().intValue());
                    assertTrue(r.getDescription().startsWith("Validation failed. "));
                    assertTrue(r.getErrors().size() > 0);
                });
    }

    @Test
    public void internalError() throws Exception {
        final String internalErrorMessage = "Connection timeout";
        when(sender.push(any(Application.class), any(String.class), any(Device.class)))
                .thenThrow(new IOException(internalErrorMessage));

        mockMvc.perform(post(senderPath)
                .content(
                        mapper.writeValueAsString(
                                new CreateMessageRequest()
                                        .withApplication(application)
                                        .withDevice(device)
                                        .withContent("hello")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(mvcResult -> {
                    Response r = Utils.deserializeFrom(
                            mvcResult
                                    .getResponse()
                                    .getContentAsString(),
                            Response.class);
                    assertEquals(201, r.getCode().intValue());
                    assertEquals(internalErrorMessage, r.getDescription());
                });
    }

    @Test
    public void pushWooshLogicFailed() throws Exception {
        final String logicErrorMessage = "Unable to send push notification.";
        when(sender.push(any(Application.class), any(String.class), any(Device.class)))
                .thenThrow(new IOException(logicErrorMessage));

        mockMvc.perform(post(senderPath)
                .content(
                        mapper.writeValueAsString(
                                new CreateMessageRequest()
                                        .withApplication(application)
                                        .withDevice(device)
                                        .withContent("hello")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(mvcResult -> {
                    Response r = Utils.deserializeFrom(
                            mvcResult
                                    .getResponse()
                                    .getContentAsString(),
                            Response.class);
                    assertEquals(201, r.getCode().intValue());
                    assertEquals(logicErrorMessage, r.getDescription());
                });
    }

    @Test
    public void registryValidationSuccess() throws Exception {
        final String successMessage = "OK";
        final String messageId = "XXXX-YYYY";
        when(sender.push(any(Application.class), any(String.class), any(Device.class)))
                .thenReturn(new PushReply(200, successMessage, messageId));

        mockMvc.perform(post(senderPath)
                .content(
                        mapper.writeValueAsString(
                                new CreateMessageRequest()
                                        .withApplication(application)
                                        .withDevice(device)
                                        .withContent("hello")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    CreateMessageResponse r = Utils.deserializeFrom(
                            mvcResult
                                    .getResponse()
                                    .getContentAsString(),
                            CreateMessageResponse.class);
                    assertEquals(200, r.getCode().intValue());
                    assertEquals(successMessage, r.getDescription());
                    assertEquals(messageId, r.getMessageId());
                });
    }
}

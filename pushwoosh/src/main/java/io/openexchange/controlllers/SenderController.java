package io.openexchange.controlllers;

import io.openexchange.pojos.api.CreateMessageRequest;
import io.openexchange.pojos.api.Response;
import io.openexchange.pushwoosh.PushWooshResponseException;
import io.openexchange.services.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static io.openexchange.controlllers.ErrorBuilder.buildPushWooshError;

@RestController
public class SenderController {
    @Autowired
    private Sender sender;

    @RequestMapping(path = "/sender/push", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> add(@Valid @RequestBody CreateMessageRequest request, Errors errors) throws IOException {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ErrorBuilder.buildValidationErrors(errors));
        }
        try {
            List<String> messageIds = sender.push(
                    request.getApplication(),
                    request.getContent(),
                    request.getDevice());
        } catch (PushWooshResponseException ex) {
            return buildPushWooshError(ex);
        }
        return ResponseEntity.ok().build();
    }
}

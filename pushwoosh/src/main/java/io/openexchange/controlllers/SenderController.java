package io.openexchange.controlllers;

import io.openexchange.pojos.api.CreateMessagePayload;
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

import static io.openexchange.controlllers.ValidationErrorBuilder.buildError;

@RestController
public class SenderController {
    @Autowired
    private Sender sender;

    @RequestMapping(path = "/sender/push", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity add(@Valid @RequestBody CreateMessagePayload payload, Errors errors) throws IOException {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ValidationErrorBuilder.fromBindingErrors(errors));
        }
        try {
            List<String> messageIds = sender.push(
                    payload.getApplication(),
                    payload.getContent(),
                    payload.getDevice());

        } catch (PushWooshResponseException ex) {
            return buildError(ex);
        }
        return ResponseEntity.ok().build();
    }
}

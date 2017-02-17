package io.openexchange.controlllers;

import io.openexchange.pojos.api.RegisterDevicePayload;
import io.openexchange.pojos.api.RegistryUserPayload;
import io.openexchange.pojos.api.ResponsePayload;
import io.openexchange.pushwoosh.PushWooshResponseException;
import io.openexchange.services.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@RestController
public class RegistryController {
    @Autowired
    private Registry registry;

    @RequestMapping(path = "/registry/add", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity add(@Valid @RequestBody RegisterDevicePayload payload, Errors errors) throws IOException {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ValidationErrorBuilder.fromBindingErrors(errors));
        }
        try {
            if (!registry.add(payload.getApplication(), payload.getDevice())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (PushWooshResponseException ex) {
            return buildError(ex);
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/registry/remove", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity remove(@Valid @RequestBody RegisterDevicePayload payload, Errors errors) throws IOException {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ValidationErrorBuilder.fromBindingErrors(errors));
        }
        try {
            if (!registry.remove(payload.getApplication(), payload.getDevice())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (PushWooshResponseException ex) {
            return buildError(ex);
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(path = "/registry/assign", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity assign(@Valid @RequestBody RegistryUserPayload payload, Errors errors) throws IOException {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ValidationErrorBuilder.fromBindingErrors(errors));
        }
        try {
            if (!registry.assign(payload.getUser(), payload.getApplication(), payload.getDevice())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (PushWooshResponseException ex) {
            return buildError(ex);
        }
        return ResponseEntity.ok().build();
    }

    private static ResponseEntity buildError(PushWooshResponseException ex){
        ResponsePayload rp = new ResponsePayload().withCode(ex.getCode()).withDescription(ex.getMessage());
        return ResponseEntity.ok(rp);
    }
}

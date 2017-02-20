package io.openexchange.controlllers;

import io.openexchange.pojos.api.*;
import io.openexchange.pushwoosh.PushWooshResponseException;
import io.openexchange.services.Registry;
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

import static io.openexchange.controlllers.ErrorBuilder.buildError;
import static io.openexchange.controlllers.ErrorBuilder.buildPushWooshError;

@RestController
public class RegistryController {
    @Autowired
    private Registry registry;

    @RequestMapping(
            path = "/registry/add",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> add(@Valid @RequestBody RegisterDeviceRequest request, Errors errors) throws IOException {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ErrorBuilder.buildValidationErrors(errors));
        }
        try {
            if (!registry.add(request.getApplication(), request.getDevice())) {
                throw new PushWooshResponseException(201, "Unable to register a device to PushWoosh application.");
            }
        } catch (PushWooshResponseException ex) {
            return buildPushWooshError(ex);
        } catch (Throwable t) {
            return buildError(t);
        }
        return ResponseEntity.ok().body(new RegisterDeviceResponse().withCode(200).withDescription("OK"));
    }

    @RequestMapping(
            path = "/registry/remove",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> remove(@Valid @RequestBody RegisterDeviceRequest request, Errors errors) throws IOException {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ErrorBuilder.buildValidationErrors(errors));
        }
        try {
            if (!registry.remove(request.getApplication(), request.getDevice())) {
                throw new PushWooshResponseException(201, "Unable to unregister a device from PushWoosh application.");
            }
        } catch (PushWooshResponseException ex) {
            return buildPushWooshError(ex);
        } catch (Throwable t) {
            return buildError(t);
        }
        return ResponseEntity.ok().body(new RegisterDeviceResponse().withCode(200).withDescription("OK"));
    }

    @RequestMapping(
            path = "/registry/assign",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> assign(@Valid @RequestBody RegisterUserRequest request, Errors errors) throws IOException {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ErrorBuilder.buildValidationErrors(errors));
        }
        try {
            if (!registry.assign(request.getUser(), request.getApplication(), request.getDevice())) {
                throw new PushWooshResponseException(201, "Unable to assign a user to PushWoosh application.");
            }
        } catch (PushWooshResponseException ex) {
            return buildPushWooshError(ex);
        } catch (Throwable t) {
            return buildError(t);
        }
        return ResponseEntity.ok().body(new RegisterUserResponse().withCode(200).withDescription("OK"));
    }
}

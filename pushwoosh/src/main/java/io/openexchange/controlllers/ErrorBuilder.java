package io.openexchange.controlllers;

import io.openexchange.pojos.api.Response;
import io.openexchange.pojos.api.ValidationErrorResponse;
import io.openexchange.pushwoosh.PushWooshResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

class ErrorBuilder {
    static ValidationErrorResponse buildValidationErrors(Errors errors) {
        ValidationErrorResponse validationError = new ValidationErrorResponse()
                .withCode(201)
                .withDescription("Validation failed. " + errors.getErrorCount() + " error(s)");
        for (ObjectError objectError : errors.getAllErrors()) {
            validationError.getErrors().add(objectError.getDefaultMessage());
        }
        return validationError;
    }

    static ResponseEntity<Response> buildPushWooshError(PushWooshResponseException ex) {
        Response rp = new Response().withCode(ex.getCode()).withDescription(ex.getMessage());
        return ResponseEntity.ok(rp);
    }

    static ResponseEntity<Response> buildError(Throwable t) {
        Response rp = new Response().withCode(201).withDescription(t.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rp);
    }
}

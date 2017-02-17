package io.openexchange.controlllers;

import io.openexchange.pojos.api.ResponsePayload;
import io.openexchange.pojos.validation.ValidationError;
import io.openexchange.pushwoosh.PushWooshResponseException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

class ValidationErrorBuilder {
    static ValidationError fromBindingErrors(Errors errors) {
        ValidationError validationError = new ValidationError().withErrorMessage("Validation failed. " + errors.getErrorCount() + " error(s)");
        for (ObjectError objectError : errors.getAllErrors()) {
            validationError.getErrors().add(objectError.getDefaultMessage());
        }
        return validationError;
    }

    static ResponseEntity buildError(PushWooshResponseException ex){
        ResponsePayload rp = new ResponsePayload().withCode(ex.getCode()).withDescription(ex.getMessage());
        return ResponseEntity.ok(rp);
    }
}

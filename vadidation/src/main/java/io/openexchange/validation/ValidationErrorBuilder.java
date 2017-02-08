package io.openexchange.validation;

import io.openexchange.pojos.ValidationError;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

public class ValidationErrorBuilder {
    public static ValidationError fromBindingErrors(Errors errors) {
        ValidationError validationError = new ValidationError().withErrorMessage("Validation failed. " + errors.getErrorCount() + " error(s)");
        for (ObjectError objectError : errors.getAllErrors()) {
            validationError.getErrors().add(objectError.getDefaultMessage());
        }
        return validationError;
    }
}
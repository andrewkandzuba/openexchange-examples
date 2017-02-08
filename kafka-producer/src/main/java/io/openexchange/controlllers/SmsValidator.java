package io.openexchange.controlllers;

import io.openexchange.pojos.Sms;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class SmsValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return aClass == Sms.class;
    }

    @Override
    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "messageId", "messageId.empty");
        ValidationUtils.rejectIfEmpty(errors, "mobileOriginate", "mobileOriginate.empty");
        ValidationUtils.rejectIfEmpty(errors, "mobileTerminate", "mobileTerminate.empty");
        ValidationUtils.rejectIfEmpty(errors, "text", "text.empty");
        ValidationUtils.rejectIfEmpty(errors, "receiveTime", "receiveTime.empty");
        //Sms sms = (Sms) o;
        //if (Objects.isNull(sms.getMessageId())) errors.rejectValue("host", "");
    }
}

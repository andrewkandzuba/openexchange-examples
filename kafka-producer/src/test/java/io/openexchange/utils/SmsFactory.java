package io.openexchange.utils;

import io.openexchange.pojos.Sms;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class SmsFactory {
    public static Sms of(String to, String from, String text) {
        return new Sms()
                .withMessageId(UUID.randomUUID())
                .withMobileTerminate(to)
                .withMobileOriginate(from)
                .withText(text)
                .withReceiveTime(Date.from(Instant.now()));
    }
}

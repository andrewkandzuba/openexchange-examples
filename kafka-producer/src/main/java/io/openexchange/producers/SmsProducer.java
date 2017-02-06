package io.openexchange.producers;

import io.openexchange.configuration.SmsChannel;
import io.openexchange.pojos.Sms;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class SmsProducer {
    @SendTo(SmsChannel.SMS)
    public Sms produce(String to, String from, String text){
        return new Sms()
                .withMessageId(UUID.randomUUID())
                .withMobileTerminate(to)
                .withMobileOriginate(from)
                .withText(text)
                .withReceiveTime(Date.from(Instant.now()));
    }
}

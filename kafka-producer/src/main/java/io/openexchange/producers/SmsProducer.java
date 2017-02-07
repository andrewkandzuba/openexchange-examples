package io.openexchange.producers;

import io.openexchange.configuration.SmsChannel;
import io.openexchange.pojos.Sms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class SmsProducer {
    private MessageChannel output;

    @Autowired
    public SmsProducer(@Qualifier(SmsChannel.SMS) MessageChannel output) {
        this.output = output;
    }

    public void produce(String to, String from, String text) {
        output.send(MessageBuilder.withPayload(of(to, from, text)).build());
    }

    private static Sms of(String to, String from, String text){
        return new Sms()
                .withMessageId(UUID.randomUUID())
                .withMobileTerminate(to)
                .withMobileOriginate(from)
                .withText(text)
                .withReceiveTime(Date.from(Instant.now()));
    }
}

package io.openexchange.producers;

import io.openexchange.pojos.Sms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class SmsProducer {
    private final Source source;

    @Autowired
    public SmsProducer(Source source) {
        this.source = source;
    }

    public void produce(String to, String from, String text) {
        source.output().send(MessageBuilder.withPayload(of(to, from, text)).build());
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

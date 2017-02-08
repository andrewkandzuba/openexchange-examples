package io.openexchange.producers;

import io.openexchange.pojos.Sms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class SmsProducer {
    private final Source source;

    @Autowired
    public SmsProducer(Source source) {
        this.source = source;
    }

    public boolean send(Sms sms){
        return source.output().send(MessageBuilder.withPayload(sms).build());
    }
}

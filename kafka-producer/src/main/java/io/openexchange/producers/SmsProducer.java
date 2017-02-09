package io.openexchange.producers;

import io.openexchange.pojos.Sms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(Processor.class)
public class SmsProducer {
    private final static Logger logger = LoggerFactory.getLogger(SmsProducer.class);
    private final Source source;

    @Autowired
    public SmsProducer(Source source) {
        this.source = source;
    }

    public boolean send(Sms sms){
        logger.info("Sending sms:[" + sms + "]");
        return source.output().send(MessageBuilder.withPayload(sms).build());
    }
}

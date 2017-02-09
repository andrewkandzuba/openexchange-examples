package io.openexchange.consumers;

import io.openexchange.pojos.Sms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(Sink.class)
public class SmsConsumer {
    private final static Logger logger = LoggerFactory.getLogger(SmsConsumer.class);

    @StreamListener(Sink.INPUT)
    public void receive(Sms sms){
        logger.info("Sms has been received: [" + sms + "]");
    }
}

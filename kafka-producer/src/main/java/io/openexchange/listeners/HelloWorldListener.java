package io.openexchange.listeners;

import io.openexchange.pojos.Sms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

public class HelloWorldListener {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldListener.class);

    @StreamListener(Sink.INPUT)
    public void processSms(Sms sms) {
        logger.info(sms.toString());
    }
}

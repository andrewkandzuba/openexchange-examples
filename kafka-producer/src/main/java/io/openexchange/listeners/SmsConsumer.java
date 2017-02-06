package io.openexchange.listeners;

import io.openexchange.configuration.SmsChannel;
import io.openexchange.pojos.Sms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;

public class SmsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(SmsConsumer.class);

    @StreamListener(SmsChannel.SMS)
    public void consume(Sms sms) {
        logger.info(sms.toString());
    }
}

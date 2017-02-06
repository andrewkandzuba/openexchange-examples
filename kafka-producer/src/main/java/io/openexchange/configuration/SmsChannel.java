package io.openexchange.configuration;


import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface SmsChannel {
    String SMS = "incoming.sms.t";

    @Input(SMS)
    SubscribableChannel sms();
}

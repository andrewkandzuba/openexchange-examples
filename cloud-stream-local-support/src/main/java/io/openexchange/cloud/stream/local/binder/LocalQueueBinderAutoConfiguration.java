package io.openexchange.cloud.stream.local.binder;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.messaging.MessageChannel;

@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class LocalQueueBinderAutoConfiguration {
    private Binder<MessageChannel, ?, ?> messageChannelBinder = new LocalQueueBinder();

    @Bean
    public BinderFactory binderFactory() {
        return configurationName -> LocalQueueBinderAutoConfiguration.this.messageChannelBinder;
    }
}

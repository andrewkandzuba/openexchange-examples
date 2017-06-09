package io.openexchange.cloud.stream.local.binder;

import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.messaging.MessageChannel;

public class LocalQueueBinder implements Binder<MessageChannel, ConsumerProperties, ProducerProperties> {

    @Override
    public Binding<MessageChannel> bindConsumer(String name, String group, MessageChannel inboundBindTarget, ConsumerProperties consumerProperties) {
        return null;
    }

    @Override
    public Binding<MessageChannel> bindProducer(String name, MessageChannel outboundBindTarget, ProducerProperties producerProperties) {
        return null;
    }

    private static class LocalQueueBinding implements Binding<MessageChannel> {

        @Override
        public void unbind() {

        }
    }

    private static class DIBinder {
        
    }
}

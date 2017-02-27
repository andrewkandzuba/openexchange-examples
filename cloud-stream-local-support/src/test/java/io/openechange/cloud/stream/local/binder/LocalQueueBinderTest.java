package io.openechange.cloud.stream.local.binder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.Transformer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        LocalQueueBinderTest.MyProcessor.class
})
@TestPropertySource("classpath:test.properties")
@DirtiesContext
public class LocalQueueBinderTest {
/*    @Autowired
    @Qualifier(Processor.OUTPUT)
    private MessageChannel outboundMessageChannel;

    @Autowired
    @Qualifier(Processor.INPUT)
    private MessageChannel inboundMessageChannel;

    @Autowired
    private BinderFactory binderFactory;*/

    @Test
    public void context() throws Exception {
    }

    @SpringBootApplication
    @EnableBinding(Processor.class)
    public static class MyProcessor {

        @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
        public String transform(String in) {
            return in + " world";
        }
    }
}

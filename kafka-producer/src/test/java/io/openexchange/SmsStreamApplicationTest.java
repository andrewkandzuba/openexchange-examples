package io.openexchange;

import io.openexchange.pojos.Sms;
import io.openexchange.producers.SmsProducer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@TestPropertySource(locations = "classpath:test.properties")
public class SmsStreamApplicationTest {
    @Autowired
    private Source source;
    @Autowired
    private Sink sink;
    @Autowired
    private SmsProducer smsProducer;

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("spring.cloud.stream.kafka.binder.defaultZkPort", Integer.toString(embeddedKafka.getZookeeper().port()));
        System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getBrokersAsString());

        System.setProperty("spring.cloud.stream.bindings.output.destination", "smsTopic");
        System.setProperty("spring.cloud.stream.bindings.input.destination", "smsTopic");
        System.setProperty("spring.cloud.stream.bindings.input.group", "smsGroup");
    }

    @Test
    public void contextLoads() {
        assertNotNull(this.source.output());
        assertNotNull(this.sink.input());
    }

    @Test
    public void testSendReceive() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        sink.input().subscribe(message -> {
            assertTrue(message.getPayload() instanceof Sms);
            Sms sms = (Sms) message.getPayload();
            assertEquals("+3725811223344", sms.getMobileTerminate());
            assertEquals("+3725844332211", sms.getMobileOriginate());
            assertEquals("Test sms", sms.getText());
            assertNotNull(sms.getMessageId());
            assertNotNull(sms.getReceiveTime());
            latch.countDown();
        });
        smsProducer.produce("+3725811223344", "+3725844332211", "Test sms");
        assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    }
}

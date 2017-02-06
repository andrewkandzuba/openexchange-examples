package io.openexchange;

import io.openexchange.configuration.SmsChannel;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@WebMvcTest(KafkaProducerApplication.class)
@DirtiesContext
@TestPropertySource(locations = "classpath:test.properties")
public class SmsStreamApplicationTest {
    @Autowired
    private SmsChannel channel;

    @ClassRule
    // Alternative: docker run -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=`docker-machine ip \`docker-machine active\`` --env ADVERTISED_PORT=9092 spotify/kafka
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("spring.cloud.stream.kafka.binder.defaultZkPort",
                Integer.toString(embeddedKafka.getZookeeper().port()));
    }

    @Test
    public void contextLoads() {
        assertNotNull(this.channel.sms());
    }
}

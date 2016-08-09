package gladiator.kafka;


import dominus.web.GlobalConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

@Profile("kafka")
@Configuration
public class KafkaConfig extends GlobalConfig {

    @Bean(name = "kafka-producer")
    public KafkaProducerService kafkaProducerService() {
        Properties kafkaProducerProps = new Properties();
        kafkaProducerProps.put("bootstrap.servers", env.getProperty("kafka.bootstrap.servers"));
        kafkaProducerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProducerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProducerProps.put("acks", "all");
        kafkaProducerProps.put("batch.size", 0);
        kafkaProducerProps.put("retries", 1);

        KafkaProducerService kafkaProducerService = new KafkaProducerService(kafkaProducerProps, env.getProperty("kafka.topic"), true);
        return kafkaProducerService;
    }
}

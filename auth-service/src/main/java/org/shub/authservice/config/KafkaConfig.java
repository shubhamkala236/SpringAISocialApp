package org.shub.authservice.config;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Equivalent of the AddMassTransit(x => { x.UsingRabbitMq(...) }) block in
 * Program.cs, but for Kafka. Spring Kafka's auto-configuration (from
 * spring-kafka + the spring.kafka.* properties in application.yml) already
 * wires the ProducerFactory/KafkaTemplate beans - this class only declares
 * the topics auth-service cares about, so they exist with sane defaults
 * even before Kafka's auto-topic-creation (which you generally want
 * disabled in real clusters) kicks in.
 *
 * Only auth-service's own topics are declared here. Each service that
 * publishes events should declare its own topics the same way.
 */

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put("spring.json.add.type.headers", false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name("user.registered")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userDeletedTopic() {
        return TopicBuilder.name("user.deleted")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

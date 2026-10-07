package com.payguard.config;

import com.payguard.service.TransactionService.TransactionEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import java.util.HashMap;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic transactionsTopic() { return new NewTopic("transactions", 3, (short) 1); }

    @Bean
    public ProducerFactory<String, TransactionEvent> producerFactory(KafkaProperties properties) {
        var config = new HashMap<String, Object>(properties.buildProducerProperties());
        config.put("key.serializer", StringSerializer.class);
        config.put("value.serializer", JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, TransactionEvent> kafkaTemplate(ProducerFactory<String, TransactionEvent> factory) {
        return new KafkaTemplate<>(factory);
    }
}

package com.payguard.config;

import com.payguard.event.TransactionEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import java.util.HashMap;

@Configuration
public class KafkaConfig {
    @Bean NewTopic transactionsTopic(){return new NewTopic("transactions",3,(short)1);}
    @Bean NewTopic transactionsDltTopic(){return new NewTopic("transactions.DLT",1,(short)1);}

    @Bean ProducerFactory<String,TransactionEvent> producerFactory(KafkaProperties properties){
        var config=new HashMap<String,Object>(properties.buildProducerProperties());
        config.put("key.serializer", StringSerializer.class); config.put("value.serializer", JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS,false);
        return new DefaultKafkaProducerFactory<>(config);
    }
    @Bean KafkaTemplate<String,TransactionEvent> kafkaTemplate(ProducerFactory<String,TransactionEvent> factory){return new KafkaTemplate<>(factory);}

    @Bean ConsumerFactory<String,TransactionEvent> consumerFactory(KafkaProperties properties){
        var config=new HashMap<String,Object>(properties.buildConsumerProperties());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES,"com.payguard.event");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,TransactionEvent.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS,false);
        return new DefaultKafkaConsumerFactory<>(config);
    }
    @Bean ConcurrentKafkaListenerContainerFactory<String,TransactionEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String,TransactionEvent> factory, KafkaTemplate<String,TransactionEvent> template){
        var f=new ConcurrentKafkaListenerContainerFactory<String,TransactionEvent>();
        f.setConsumerFactory(factory);
        var recoverer=new DeadLetterPublishingRecoverer(template);
        f.setCommonErrorHandler(new DefaultErrorHandler(recoverer,new FixedBackOff(1000L,2L)));
        return f;
    }
}

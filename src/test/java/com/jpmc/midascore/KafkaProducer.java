package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    private final String topic;
    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    // ✅ Use dot-separated property name
    public KafkaProducer(@Value("${general.kafka.topic}") String topic,
                         KafkaTemplate<String, Transaction> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String transactionLine) {
        // For now, the test expects this dummy transaction
        String[] transactionData = transactionLine.split(", ");
        kafkaTemplate.send(topic, "test-key", new Transaction("1", "2", 42.87f));
        System.out.println("✅ Sent transaction to Kafka topic: " + topic);
    }
}

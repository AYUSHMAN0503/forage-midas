package com.jpmc.midascore;

import com.jpmc.midascore.component.DatabaseConduit;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class KafkaTransactionListener {

    private final DatabaseConduit databaseConduit;

    public KafkaTransactionListener(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }

    @KafkaListener(topics = "${general.kafka.topic}", groupId = "midas-core-group")
    public void handleTransaction(Transaction transaction) {
        databaseConduit.processTransaction(
                transaction.getSenderId(),
                transaction.getRecipientId(),
                BigDecimal.valueOf(transaction.getAmount())
        );

        // Print updated balance after each processed transaction
        databaseConduit.printWaldorfBalance();
    }
}

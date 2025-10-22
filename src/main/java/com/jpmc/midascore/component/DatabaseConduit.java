package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DatabaseConduit {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public DatabaseConduit(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void processTransaction(String senderId, String recipientId, BigDecimal amount) {
        try {
            // Find sender and recipient by numeric ID
            UserRecord sender = userRepository.findById(Long.parseLong(senderId));
            UserRecord recipient = userRepository.findById(Long.parseLong(recipientId));

            if (sender == null || recipient == null) {
                System.out.println("❌ Invalid sender or recipient — discarding transaction");
                return;
            }

            if (sender.getBalance() < amount.floatValue()) {
                System.out.println("❌ Insufficient funds — discarding transaction");
                return;
            }

            // Update balances
            sender.setBalance(sender.getBalance() - amount.floatValue());
            recipient.setBalance(recipient.getBalance() + amount.floatValue());

            userRepository.save(sender);
            userRepository.save(recipient);
            transactionRepository.save(new TransactionRecord(sender, recipient, amount));

            System.out.println("✅ Transaction recorded successfully");

        } catch (Exception e) {
            System.out.println("⚠️ Transaction processing failed: " + e.getMessage());
        }
    }

    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }

    // Print Waldorf’s current balance
    public void printWaldorfBalance() {
        userRepository.findAll().forEach(user -> {
            if (user.getName().equalsIgnoreCase("waldorf")) {
                System.out.println("🎯 Waldorf current balance: " + Math.floor(user.getBalance()));
            }
        });
    }
}

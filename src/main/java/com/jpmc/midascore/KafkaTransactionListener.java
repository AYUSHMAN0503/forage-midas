package com.jpmc.midascore;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@EnableKafka
public class KafkaTransactionListener {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";

    @Transactional
    @KafkaListener(topics = "transactions", groupId = "midas-core")
    public void listen(Transaction transaction) {
        try {
            long senderIdNum = Long.parseLong(transaction.getSenderId());
            long recipientIdNum = Long.parseLong(transaction.getRecipientId());
            double amountVal = transaction.getAmount();
            BigDecimal amount = BigDecimal.valueOf(amountVal);

            String senderKey = resolveUserKey(senderIdNum);
            String recipientKey = resolveUserKey(recipientIdNum);

            Optional<UserRecord> senderOpt = userRepository.findById(senderKey);
            Optional<UserRecord> recipientOpt = userRepository.findById(recipientKey);

            if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
                System.out.println("[Discard] Invalid IDs. sender=" + senderKey + " recipient=" + recipientKey);
                return;
            }

            UserRecord sender = senderOpt.get();
            UserRecord recipient = recipientOpt.get();

            BigDecimal senderBal = BigDecimal.valueOf(sender.getBalance());
            BigDecimal recipientBal = BigDecimal.valueOf(recipient.getBalance());

            if (senderBal.compareTo(amount) < 0) {
                System.out.println("[Discard] Insufficient funds for " + sender.getUserId());
                return;
            }

            // ✅ Call Incentive API
            Incentive incentiveResponse = restTemplate.postForObject(INCENTIVE_API_URL, transaction, Incentive.class);
            double incentive = (incentiveResponse != null) ? incentiveResponse.getAmount() : 0.0;

            // ✅ Update balances — add incentive only once via record, not to recipient balance
            BigDecimal newSenderBal = senderBal.subtract(amount);
            BigDecimal newRecipientBal = recipientBal.add(amount);

            sender.setBalance(newSenderBal.doubleValue());
            recipient.setBalance(newRecipientBal.doubleValue());

            // ✅ Save transaction with incentive recorded (recipient will get it applied separately)
            TransactionRecord record = new TransactionRecord(sender, recipient, amount.doubleValue(), incentive, LocalDateTime.now());
            transactionRepository.save(record);
            userRepository.save(sender);
            userRepository.save(recipient);

            System.out.println("[OK] Tx saved: sender=" + sender.getUserId() +
                    " recipient=" + recipient.getUserId() +
                    " amount=" + amount + " incentive=" + incentive);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String resolveUserKey(long id) {
        return switch ((int) id) {
            case 1 -> "waldorf";
            case 2 -> "statler";
            case 3 -> "gonzo";
            case 4 -> "kermit";
            case 5 -> "wilbur"; // include wilbur for task four
            default -> String.valueOf(id);
        };
    }
}

package com.jpmc.midascore;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class KafkaConsumer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void consume(Transaction transaction) {
        System.out.println("Received transaction: " + transaction);

        // Validate sender and recipient
        Optional<UserRecord> senderOpt = userRepository.findById(transaction.getSenderId());
        Optional<UserRecord> recipientOpt = userRepository.findById(transaction.getRecipientId());

        if (senderOpt.isPresent() && recipientOpt.isPresent()) {
            UserRecord sender = senderOpt.get();
            UserRecord recipient = recipientOpt.get();

            System.out.println("💰 Before -> Sender balance: " + sender.getBalance() + ", Recipient balance: " + recipient.getBalance());

            if (sender.getBalance() >= transaction.getAmount()) {

                String url = "http://localhost:8080/incentive";
                Incentive incentive = restTemplate.postForObject(url, transaction, Incentive.class);
                double incentiveAmount = incentive != null ? incentive.getAmount() : 0.0;


                // ✅ Update balances
                sender.setBalance(sender.getBalance() - transaction.getAmount());
                recipient.setBalance(recipient.getBalance() + transaction.getAmount() + (float)incentiveAmount);

                // Save updated users
                userRepository.save(sender);
                userRepository.save(recipient);

                // ✅ Record transaction in DB
                TransactionRecord record = new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount);
                transactionRecordRepository.save(record);

                System.out.println("✅ Transaction successful! Amount: " + transaction.getAmount());
                System.out.println("💰 After -> Sender balance: " + sender.getBalance() + ", Recipient balance: " + recipient.getBalance());
            } else {
                System.out.println("❌ Invalid transaction (insufficient funds): " + transaction);
            }
        } else {
            System.out.println("❌ Invalid transaction (user not found): " + transaction);
        }
    }
}

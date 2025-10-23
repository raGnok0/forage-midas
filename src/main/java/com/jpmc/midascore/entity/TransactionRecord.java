package com.jpmc.midascore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float amount;
    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private UserRecord sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private UserRecord recipient;

    public TransactionRecord() {}


    // Incentive entity
    public double incentive;


    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount, double incentive) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.incentive = incentive;
    }

    // Getters and setters
    public Long getId() { return id; }
    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }
    public UserRecord getSender() { return sender; }
    public UserRecord getRecipient() { return recipient; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getIncentive() { return incentive; }
    public void setIncentive(double incentive) { this.incentive = incentive; }
}

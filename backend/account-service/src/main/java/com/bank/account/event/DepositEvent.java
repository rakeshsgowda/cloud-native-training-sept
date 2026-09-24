package com.bank.account.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DepositEvent {
    private String eventId;
    private Long accountId;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private LocalDateTime timestamp;

    public DepositEvent() {}

    public DepositEvent(Long accountId, BigDecimal amount, BigDecimal newBalance) {
        this.eventId = UUID.randomUUID().toString();
        this.accountId = accountId;
        this.amount = amount;
        this.newBalance = newBalance;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getNewBalance() { return newBalance; }
    public void setNewBalance(BigDecimal newBalance) { this.newBalance = newBalance; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
package com.bank.account.listener;

import com.bank.account.event.DepositEvent;
import com.bank.account.event.WithdrawalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "deposit-events", groupId = "notification-service")
    public void onDeposit(DepositEvent event) {
        if (!processedEventIds.add(event.getEventId())) {
            log.warn("Duplicate deposit event ignored: {}", event.getEventId());
            return;
        }
        log.info("Notify: deposit of {} on account {} — new balance {}",
                event.getAmount(), event.getAccountId(), event.getNewBalance());
    }

    @KafkaListener(topics = "withdrawal-events", groupId = "notification-service")
    public void onWithdrawal(WithdrawalEvent event) {
        if (!processedEventIds.add(event.getEventId())) {
            log.warn("Duplicate withdrawal event ignored: {}", event.getEventId());
            return;
        }
        log.info("Notify: withdrawal of {} on account {} — new balance {}",
                event.getAmount(), event.getAccountId(), event.getNewBalance());
    }
}
package com.bank.account.service;

import com.bank.account.client.CustomerServiceClient;
import com.bank.account.dto.CreateAccountRequest;
import com.bank.account.dto.DepositRequest;
import com.bank.account.dto.WithdrawalRequest;
import com.bank.account.entity.Account;
import com.bank.account.entity.BankingTransaction;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.bank.account.event.DepositEvent;
import com.bank.account.event.WithdrawalEvent;
import org.springframework.kafka.core.KafkaTemplate;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerServiceClient customerServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AccountService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            CustomerServiceClient customerServiceClient,
            KafkaTemplate<String, Object> kafkaTemplate) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.customerServiceClient = customerServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Create a new bank account.
     */
    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        customerServiceClient.assertCustomerExists(request.getCustomerId());
        Account account = new Account();

        account.setCustomerId(request.getCustomerId());

        account.setAccountNumber(
                generateAccountNumber()
        );

        account.setAccountType(
                request.getAccountType().toUpperCase()
        );

        account.setBalance(
                BigDecimal.ZERO
        );

        account.setStatus("ACTIVE");

        return accountRepository.save(account);
    }

    /**
     * Retrieve an account by ID.
     */
    public Account getAccount(Long accountId) {

        return accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found: " + accountId
                        )
                );
    }

    /**
     * Retrieve all accounts.
     */
    public List<Account> getAllAccounts() {

        return accountRepository.findAll();
    }
    /**
     * Retrieve all accounts belonging to a customer.
     */
    public List<Account> getCustomerAccounts(Long customerId) {

        return accountRepository.findByCustomerId(customerId);
    }

    /**
     * Deposit money into an account.
     */
    @Transactional
    public Account deposit(
            Long accountId,
            DepositRequest request) {

        Account account = getAccount(accountId);

        validateActiveAccount(account);

        BigDecimal amount = request.getAmount();

        BigDecimal newBalance =
                account.getBalance().add(amount);

        account.setBalance(newBalance);

        Account savedAccount =
                accountRepository.save(account);

        createTransaction(
                account,
                "DEPOSIT",
                amount,
                newBalance
        );

        DepositEvent event = new DepositEvent(accountId, amount, newBalance);
        kafkaTemplate.send("deposit-events", accountId.toString(), event);

        return savedAccount;
    }

    /**
     * Withdraw money from an account.
     */
    @Transactional
    public Account withdraw(
            Long accountId,
            WithdrawalRequest request) {

        Account account = getAccount(accountId);

        validateActiveAccount(account);

        BigDecimal amount = request.getAmount();

        if (account.getBalance().compareTo(amount) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient balance"
            );
        }

        BigDecimal newBalance =
                account.getBalance().subtract(amount);

        account.setBalance(newBalance);

        Account savedAccount =
                accountRepository.save(account);

        createTransaction(
                account,
                "WITHDRAWAL",
                amount,
                newBalance
        );

        WithdrawalEvent event = new WithdrawalEvent(accountId, amount, newBalance);
        kafkaTemplate.send("withdrawal-events", accountId.toString(), event);

        return savedAccount;
    }

    /**
     * Retrieve transaction history for an account.
     */
    public List<BankingTransaction> getTransactions(
            Long accountId) {

        // Make sure account exists
        getAccount(accountId);

        return transactionRepository
                .findByAccountIdOrderByTransactionTimeDesc(
                        accountId
                );
    }

    /**
     * Create transaction record.
     */
    private void createTransaction(
            Account account,
            String transactionType,
            BigDecimal amount,
            BigDecimal balanceAfter) {

        BankingTransaction transaction =
                new BankingTransaction();

        transaction.setAccountId(account.getId());

        transaction.setTransactionType(
                transactionType
        );

        transaction.setAmount(amount);

        transaction.setBalanceAfter(
                balanceAfter
        );

        transaction.setTransactionTime(
                LocalDateTime.now()
        );

        transactionRepository.save(transaction);
    }

    /**
     * Validate account status.
     */
    private void validateActiveAccount(
            Account account) {

        if (!"ACTIVE".equalsIgnoreCase(
                account.getStatus())) {

            throw new IllegalArgumentException(
                    "Account is not active"
            );
        }
    }

    /**
     * Generate a simple account number.
     *
     * This is sufficient for the training application.
     * A production banking application would use a
     * controlled account-number generation strategy.
     */
    private String generateAccountNumber() {

        return "ACC" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();
    }
}


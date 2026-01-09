package com.myfinance.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myfinance.api.model.Subscription;
import com.myfinance.api.model.Transaction;
import com.myfinance.api.repository.SubscriptionRepository;
import com.myfinance.api.repository.TransactionRepository;
import com.myfinance.api.repository.AccountRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {

    @Autowired private SubscriptionRepository subscriptionRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private AccountRepository accountRepository; 
    @Scheduled(cron = "0 0 8 * * ?") 
    @Transactional
    public void processSubscriptions() {
        LocalDate today = LocalDate.now();
        List<Subscription> dues = subscriptionRepository.findByNextPaymentDateLessThanEqual(today);

        for (Subscription sub : dues) {
            createTransactionForSubscription(sub);
            updateNextPaymentDate(sub);
        }
        
        if (!dues.isEmpty()) {
            System.out.println("✅ Procesadas " + dues.size() + " suscripciones.");
        }
    }

    private void createTransactionForSubscription(Subscription sub) {
        Transaction tx = new Transaction();
        tx.setAmount(sub.getAmount());
        tx.setDescription("Pago recurrente: " + sub.getName());
        tx.setType("GASTO");
        tx.setDate(LocalDate.now());
        tx.setUser(sub.getUser());
        tx.setAccount(sub.getAccount());
        tx.setCategory(sub.getCategory());

        transactionRepository.save(tx);
        
        sub.getAccount().setCurrentBalance(
            sub.getAccount().getCurrentBalance().subtract(sub.getAmount())
        );
        accountRepository.save(sub.getAccount());
    }

    private void updateNextPaymentDate(Subscription sub) {
        LocalDate nextDate = sub.getNextPaymentDate();
        if (sub.getFrequency() == Subscription.Frequency.MONTHLY) {
            nextDate = nextDate.plusMonths(1);
        } else {
            nextDate = nextDate.plusYears(1);
        }
        sub.setNextPaymentDate(nextDate);
        subscriptionRepository.save(sub);
    }
}
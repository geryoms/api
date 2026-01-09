package com.myfinance.api.service;

import com.myfinance.api.model.Account;
import com.myfinance.api.model.Subscription;
import com.myfinance.api.model.Transaction;
// Eliminamos imports de Enums externos que daban problemas
import com.myfinance.api.repository.AccountRepository;
import com.myfinance.api.repository.SubscriptionRepository;
import com.myfinance.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void processSubscriptions() {
        log.info("⏰ Iniciando procesamiento diario de suscripciones...");

        LocalDate today = LocalDate.now();
        List<Subscription> dueSubscriptions = subscriptionRepository.findByNextPaymentDateLessThanEqual(today);

        if (dueSubscriptions.isEmpty()) {
            log.info("✅ No hay suscripciones para cobrar hoy.");
            return;
        }

        log.info("💳 Encontradas {} suscripciones para cobrar.", dueSubscriptions.size());

        for (Subscription sub : dueSubscriptions) {
            try {
                processSingleSubscription(sub);
            } catch (Exception e) {
                log.error("❌ Error procesando suscripción ID: " + sub.getId(), e);
            }
        }
        log.info("🏁 Procesamiento finalizado.");
    }

    private void processSingleSubscription(Subscription sub) {
        Transaction tx = new Transaction();
        tx.setDescription("Suscripción: " + sub.getName());
        tx.setAmount(sub.getAmount());
        
        tx.setType("GASTO"); 
        
        tx.setDate(LocalDate.now());
        
        tx.setAccount(sub.getAccount());
        tx.setCategory(sub.getCategory());
        tx.setUser(sub.getUser());

        transactionRepository.save(tx);

        Account account = sub.getAccount();
        if (account != null) {
            BigDecimal currentBalance = account.getCurrentBalance();
            BigDecimal amountToSubtract = sub.getAmount();
            
            BigDecimal newBalance = currentBalance.subtract(amountToSubtract);
            
            account.setCurrentBalance(newBalance);
            accountRepository.save(account);
        }

        LocalDate nextDate = calculateNextPaymentDate(sub.getNextPaymentDate(), sub.getFrequency());
        
        sub.setNextPaymentDate(nextDate);
        subscriptionRepository.save(sub);

        log.info("   -> Cobrada: {} | Importe: {} | Próxima: {}", sub.getName(), sub.getAmount(), nextDate);
    }

    private LocalDate calculateNextPaymentDate(LocalDate current, Subscription.Frequency frequency) {
        if (frequency == Subscription.Frequency.YEARLY) {
            return current.plusYears(1);
        }
        return current.plusMonths(1);
    }
}
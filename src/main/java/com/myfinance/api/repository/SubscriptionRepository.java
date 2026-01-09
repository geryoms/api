package com.myfinance.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;
import com.myfinance.api.model.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId);
    
    // Encuentra las suscripciones cuya fecha de pago es hoy o ya pasó (por si el servidor estuvo apagado)
    List<Subscription> findByNextPaymentDateLessThanEqual(LocalDate date);
}
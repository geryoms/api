package com.myfinance.api.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.myfinance.api.model.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    List<Subscription> findByUserId(Long userId);
}
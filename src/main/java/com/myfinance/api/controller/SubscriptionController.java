package com.myfinance.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myfinance.api.model.Subscription;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.SubscriptionRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController extends BaseController {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @PostMapping
    public ResponseEntity<Subscription> createSubscription(@Valid @RequestBody Subscription subscription) {
        User currentUser = getCurrentUser();
        subscription.setUser(currentUser);
        
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        return ResponseEntity.ok(savedSubscription);
    }

    @GetMapping
    public List<Subscription> getSubscriptions() {
        User currentUser = getCurrentUser();
        return subscriptionRepository.findByUserId(currentUser.getId());
    }
}
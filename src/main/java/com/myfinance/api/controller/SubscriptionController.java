package com.myfinance.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.myfinance.api.model.Subscription;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.SubscriptionRepository;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController extends BaseController {

    @Autowired private SubscriptionRepository subscriptionRepository;

    @GetMapping
    public List<Subscription> getMySubscriptions() {
        return subscriptionRepository.findByUserId(getCurrentUser().getId());
    }

   @PostMapping
    public Subscription createSubscription(@RequestBody Subscription sub) {
        User currentUser = getCurrentUser(); 
        
        sub.setUser(currentUser); 
        
        if (sub.getNextPaymentDate() == null) {
            sub.setNextPaymentDate(sub.getStartDate());
        }
        
        return subscriptionRepository.save(sub);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubscription(@PathVariable Long id) {
        subscriptionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
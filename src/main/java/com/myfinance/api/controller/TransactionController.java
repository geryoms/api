package com.myfinance.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.myfinance.api.model.Category;
import com.myfinance.api.model.Transaction;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.CategoryRepository;
import com.myfinance.api.repository.TransactionRepository;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    
    @GetMapping
public List<Transaction> getAllTransactions() {
   
    User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


    return transactionRepository.findByUserId(currentUser.getId());
}

   
   @PostMapping
    public Transaction createTransaction(@Valid @RequestBody Transaction transaction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        transaction.setUser(currentUser);

       if (transaction.getCategory() != null && transaction.getCategory().getId() != null) {

            Category category = categoryRepository.findById(transaction.getCategory().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            
            if (category.getUser().getId() != currentUser.getId()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Category does not belong to the current user");
            }
            
           
            transaction.setCategory(category);
        }

        return transactionRepository.save(transaction);
    }

 
   @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        log.info("User ID: {} is requesting transaction ID: {}", currentUser.getId(), id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Transaction with ID: {} not found.", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });

       
        Long transactionUserId = transaction.getUser().getId();
        log.info("Transaction {} belongs to user ID: {}", id, transactionUserId);

       
        if (!currentUser.getId().equals(transactionUserId)) {
            log.error("ACCESS DENIED: User ID {} tried to access transaction owned by user ID {}", currentUser.getId(), transactionUserId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
        }
        
        return ResponseEntity.ok(transaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @Valid @RequestBody Transaction transactionDetails) {
        User currentUser = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (transaction.getUser().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        transaction.setDescription(transactionDetails.getDescription());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setDate(transactionDetails.getDate());
        transaction.setType(transactionDetails.getType());
      

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return ResponseEntity.ok(updatedTransaction);
    }

 
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

      
        if (transaction.getUser().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        transactionRepository.delete(transaction);
        return ResponseEntity.ok().build();
    }
}
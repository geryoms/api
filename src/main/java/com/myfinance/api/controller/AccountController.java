package com.myfinance.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.myfinance.api.model.Account;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.AccountRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController extends BaseController {

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<List<Account>> getAccounts() {
        User currentUser = getCurrentUser();
        List<Account> accounts = accountRepository.findByUserId(currentUser.getId());
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
        User currentUser = getCurrentUser();
        account.setUser(currentUser);
        Account savedAccount = accountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id, @Valid @RequestBody Account accountDetails) {
        User currentUser = getCurrentUser();

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        account.setName(accountDetails.getName());
        account.setInitialBalance(accountDetails.getInitialBalance());
        
        Account updatedAccount = accountRepository.save(account);
        return ResponseEntity.ok(updatedAccount);
    }


@DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        User currentUser = getCurrentUser();

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        accountRepository.delete(account);
        
        return ResponseEntity.noContent().build();
    }

    
}
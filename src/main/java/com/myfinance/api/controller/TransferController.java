package com.myfinance.api.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.myfinance.api.model.Account;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.AccountRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transfers")
public class TransferController extends BaseController {

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping
    @Transactional    public ResponseEntity<String> transferMoney(@Valid @RequestBody TransferRequest request) {
        User currentUser = getCurrentUser();

        if (request.getOriginAccountId().equals(request.getDestinationAccountId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Origin and destination accounts cannot be the same");
        }

        Account origin = accountRepository.findById(request.getOriginAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Origin account not found"));

        Account destination = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if (!origin.getUser().getId().equals(currentUser.getId()) || 
            !destination.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only transfer between your own accounts");
        }

        BigDecimal currentOriginBalance = origin.getCurrentBalance() != null ? origin.getCurrentBalance() : BigDecimal.ZERO;
        if (currentOriginBalance.compareTo(request.getAmount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        origin.setCurrentBalance(currentOriginBalance.subtract(request.getAmount()));
        
        BigDecimal currentDestBalance = destination.getCurrentBalance() != null ? destination.getCurrentBalance() : BigDecimal.ZERO;
        destination.setCurrentBalance(currentDestBalance.add(request.getAmount()));

        accountRepository.save(origin);
        accountRepository.save(destination);

        return ResponseEntity.ok("Transfer successful");
    }
}
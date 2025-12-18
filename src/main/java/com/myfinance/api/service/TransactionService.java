package com.myfinance.api.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.myfinance.api.model.Account;
import com.myfinance.api.model.Category;
import com.myfinance.api.model.Transaction;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.AccountRepository;
import com.myfinance.api.repository.CategoryRepository;
import com.myfinance.api.repository.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Transaction createTransaction(Transaction transaction, User user) {
        transaction.setUser(user);

        if (transaction.getCategory() != null && transaction.getCategory().getId() != null) {
            Category category = categoryRepository.findById(transaction.getCategory().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            
            if (!category.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Category does not belong to the user");
            }
            transaction.setCategory(category);
        }

        if (transaction.getAccount() != null && transaction.getAccount().getId() != null) {
            Account account = accountRepository.findById(transaction.getAccount().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

            if (!account.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account does not belong to the user");
            }

            updateAccountBalance(account, transaction.getAmount(), transaction.getType(), false);
            transaction.setAccount(account);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is mandatory");
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(Long id, Transaction details, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        
        if (transaction.getAccount() != null) {
            updateAccountBalance(transaction.getAccount(), transaction.getAmount(), transaction.getType(), true);
        }

        transaction.setDescription(details.getDescription());
        transaction.setAmount(details.getAmount());
        transaction.setDate(details.getDate());
        transaction.setType(details.getType());
        

        if (transaction.getAccount() != null) {
            updateAccountBalance(transaction.getAccount(), transaction.getAmount(), transaction.getType(), false);
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        if (transaction.getAccount() != null) {
            updateAccountBalance(transaction.getAccount(), transaction.getAmount(), transaction.getType(), true);
        }

        transactionRepository.delete(transaction);
    }

    private void updateAccountBalance(Account account, BigDecimal amount, String type, boolean isReversion) {
        BigDecimal currentBalance = account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;
        
        if ("INGRESO".equalsIgnoreCase(type)) {
            if (isReversion) {
                account.setCurrentBalance(currentBalance.subtract(amount)); 
                account.setCurrentBalance(currentBalance.add(amount));
            }
        } else if ("GASTO".equalsIgnoreCase(type)) {
            if (isReversion) {
                account.setCurrentBalance(currentBalance.add(amount)); 
            } else {
                account.setCurrentBalance(currentBalance.subtract(amount)); 
            }
        }
        accountRepository.save(account);
    }
}
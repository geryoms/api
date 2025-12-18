package com.myfinance.api.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myfinance.api.config.SecurityConfig;
import com.myfinance.api.model.Account;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.AccountRepository;
import com.myfinance.api.repository.CategoryRepository;
import com.myfinance.api.repository.TransactionRepository;
import com.myfinance.api.repository.UserRepository;
import com.myfinance.api.service.JwtService;

@WebMvcTest(TransferController.class)
@Import(SecurityConfig.class)
public class TransferIntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AccountRepository accountRepository;
    @MockBean private TransactionRepository transactionRepository; 
    @MockBean private CategoryRepository categoryRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;

    private User mockUser;
    private Account originAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() throws Exception {
        mockUser = new User();
        setPrivateField(mockUser, "id", 1L);

        originAccount = new Account();
        setPrivateField(originAccount, "id", 10L);
        originAccount.setName("Banco");
        originAccount.setUser(mockUser);
        originAccount.setCurrentBalance(new BigDecimal("100.00")); 

        destinationAccount = new Account();
        setPrivateField(destinationAccount, "id", 20L);
        destinationAccount.setName("Ahorros");
        destinationAccount.setUser(mockUser);
        destinationAccount.setCurrentBalance(new BigDecimal("50.00")); 
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    void whenTransferMoney_thenBalancesAreUpdated() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setOriginAccountId(10L);
        request.setDestinationAccountId(20L);
        request.setAmount(new BigDecimal("30.00"));

        given(accountRepository.findById(10L)).willReturn(Optional.of(originAccount));
        given(accountRepository.findById(20L)).willReturn(Optional.of(destinationAccount));

        mockMvc.perform(post("/api/transfers")
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        org.mockito.Mockito.verify(accountRepository, org.mockito.Mockito.times(2)).save(org.mockito.ArgumentMatchers.any(Account.class));

        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("70.00"), originAccount.getCurrentBalance());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("80.00"), destinationAccount.getCurrentBalance());
    }
}
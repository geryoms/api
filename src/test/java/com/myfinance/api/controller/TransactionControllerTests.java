package com.myfinance.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.myfinance.api.model.Transaction;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.AccountRepository;
import com.myfinance.api.repository.CategoryRepository;
import com.myfinance.api.repository.TransactionRepository;
import com.myfinance.api.repository.UserRepository;
import com.myfinance.api.service.JwtService;
import com.myfinance.api.service.TransactionService;

@WebMvcTest(TransactionController.class)
// IMPORTANTE: Importamos TransactionService para que el test use la lógica real del servicio
// pero con los repositorios mockeados definidos abajo.
@Import({SecurityConfig.class, TransactionService.class})
public class TransactionControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Mocks de Repositorios (El TransactionService usará estos mocks)
    @MockBean private TransactionRepository transactionRepository;
    @MockBean private CategoryRepository categoryRepository;
    @MockBean private AccountRepository accountRepository;
    @MockBean private UserRepository userRepository;
    
    // Mocks de Seguridad
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;

    private User mockUser;
    private Account mockAccount;

    @BeforeEach
    void setUp() throws Exception {
        // Configuración básica del usuario
        mockUser = new User();
        setPrivateField(mockUser, "id", 1L);
        mockUser.setEmail("test@test.com");

        // Configuración de una cuenta válida
        mockAccount = new Account();
        setPrivateField(mockAccount, "id", 10L);
        mockAccount.setName("Banco Test");
        mockAccount.setUser(mockUser);
        // Inicializamos saldo para evitar NullPointerException en la lógica del servicio
        mockAccount.setCurrentBalance(new BigDecimal("100.00")); 
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    void whenPostTransaction_withValidAccount_thenCreatesTransaction() throws Exception {
        // GIVEN
        Transaction transactionRequest = new Transaction();
        transactionRequest.setDescription("Gasto Supermercado");
        transactionRequest.setAmount(new BigDecimal("50.00"));
        transactionRequest.setDate(LocalDate.now());
        transactionRequest.setType("GASTO");
        transactionRequest.setAccount(mockAccount);

        Transaction savedTransaction = new Transaction();
        setPrivateField(savedTransaction, "id", 1L);
        savedTransaction.setDescription("Gasto Supermercado");
        savedTransaction.setAccount(mockAccount);

        given(accountRepository.findById(10L)).willReturn(Optional.of(mockAccount));
        given(transactionRepository.save(any(Transaction.class))).willReturn(savedTransaction);

        // WHEN & THEN
        mockMvc.perform(post("/api/transactions")
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void whenPostTransaction_withAccountFromAnotherUser_thenForbidden() throws Exception {
        // GIVEN
        User anotherUser = new User();
        setPrivateField(anotherUser, "id", 2L);

        Account otherAccount = new Account();
        setPrivateField(otherAccount, "id", 99L);
        otherAccount.setUser(anotherUser);

        Transaction transactionRequest = new Transaction();
        transactionRequest.setDescription("Intento Hack");
        transactionRequest.setAmount(BigDecimal.TEN);
        transactionRequest.setDate(LocalDate.now());
        transactionRequest.setType("GASTO");
        transactionRequest.setAccount(otherAccount);

        given(accountRepository.findById(99L)).willReturn(Optional.of(otherAccount));

        // WHEN & THEN
        mockMvc.perform(post("/api/transactions")
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenPostIncomeTransaction_thenAccountBalanceIncreases() throws Exception {
        // GIVEN
        mockAccount.setCurrentBalance(new BigDecimal("100.00"));
        given(accountRepository.findById(10L)).willReturn(Optional.of(mockAccount));

        Transaction incomeTransaction = new Transaction();
        incomeTransaction.setDescription("Salario Extra");
        incomeTransaction.setAmount(new BigDecimal("50.00"));
        incomeTransaction.setDate(LocalDate.now());
        incomeTransaction.setType("INGRESO");
        incomeTransaction.setAccount(mockAccount);

        given(transactionRepository.save(any(Transaction.class))).willReturn(incomeTransaction);

        // WHEN
        mockMvc.perform(post("/api/transactions")
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incomeTransaction)))
                .andExpect(status().isOk());

        // THEN
        org.mockito.ArgumentCaptor<Account> accountCaptor = org.mockito.ArgumentCaptor.forClass(Account.class);
        org.mockito.Mockito.verify(accountRepository).save(accountCaptor.capture());
        
        Account updatedAccount = accountCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(
            new BigDecimal("150.00"), 
            updatedAccount.getCurrentBalance()
        );
    }

    @Test
    void whenDeleteIncomeTransaction_thenAccountBalanceDecreases() throws Exception {
        // GIVEN
        mockAccount.setCurrentBalance(new BigDecimal("150.00"));

        Transaction existingTransaction = new Transaction();
        setPrivateField(existingTransaction, "id", 1L);
        existingTransaction.setDescription("Ingreso a borrar");
        existingTransaction.setAmount(new BigDecimal("50.00"));
        existingTransaction.setType("INGRESO");
        existingTransaction.setAccount(mockAccount);
        existingTransaction.setUser(mockUser);

        given(transactionRepository.findById(1L)).willReturn(Optional.of(existingTransaction));

        // WHEN
        mockMvc.perform(delete("/api/transactions/1")
                .with(user(mockUser)))
                .andExpect(status().isOk());

        // THEN
        org.mockito.ArgumentCaptor<Account> accountCaptor = org.mockito.ArgumentCaptor.forClass(Account.class);
        org.mockito.Mockito.verify(accountRepository).save(accountCaptor.capture());

        Account updatedAccount = accountCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(
            new BigDecimal("100.00"),
            updatedAccount.getCurrentBalance()
        );
    }

    @Test
    void whenUpdateTransactionAmount_thenBalanceIsAdjusted() throws Exception {
        // GIVEN
        // Saldo inicial: 100
        mockAccount.setCurrentBalance(new BigDecimal("100.00"));

        // Transacción original: GASTO de 20
        Transaction originalTransaction = new Transaction();
        setPrivateField(originalTransaction, "id", 1L);
        originalTransaction.setDescription("Gasto Original");
        originalTransaction.setAmount(new BigDecimal("20.00"));
        originalTransaction.setType("GASTO");
        originalTransaction.setAccount(mockAccount);
        originalTransaction.setUser(mockUser);

        // Datos nuevos: GASTO de 30
        Transaction updateRequest = new Transaction();
        updateRequest.setDescription("Gasto Corregido");
        updateRequest.setAmount(new BigDecimal("30.00"));
        updateRequest.setDate(LocalDate.now());
        updateRequest.setType("GASTO");
        updateRequest.setAccount(mockAccount);

        given(transactionRepository.findById(1L)).willReturn(Optional.of(originalTransaction));
        given(transactionRepository.save(any(Transaction.class))).willReturn(updateRequest);

        // WHEN
        mockMvc.perform(put("/api/transactions/1")
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // THEN
        // Saldo esperado: 100 (inicio) + 20 (revertir gasto orig) - 30 (aplicar gasto nuevo) = 90
        org.mockito.ArgumentCaptor<Account> accountCaptor = org.mockito.ArgumentCaptor.forClass(Account.class);
        org.mockito.Mockito.verify(accountRepository, org.mockito.Mockito.atLeastOnce()).save(accountCaptor.capture());

        // Capturamos la última llamada al save
        Account finalAccountState = accountCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(
            new BigDecimal("90.00"),
            finalAccountState.getCurrentBalance()
        );
    }
}
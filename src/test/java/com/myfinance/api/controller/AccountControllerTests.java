package com.myfinance.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.math.BigDecimal;
import java.util.List;
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
import com.myfinance.api.service.JwtService;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
public class AccountControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockUser, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void whenPostAccount_thenCreateAndReturnAccount() throws Exception {
        Account accountToCreate = new Account();
        accountToCreate.setName("Nueva Cuenta");
        accountToCreate.setInitialBalance(BigDecimal.valueOf(100));

        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setName("Nueva Cuenta");
        savedAccount.setInitialBalance(BigDecimal.valueOf(100));

        given(accountRepository.save(any(Account.class))).willReturn(savedAccount);

        mockMvc.perform(post("/api/accounts")
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountToCreate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Nueva Cuenta"));
    }

    @Test
    void whenGetAccounts_thenReturnOnlyUserAccounts() throws Exception {
        Account account1 = new Account();
        account1.setId(1L);
        account1.setName("Cuenta 1");
        account1.setUser(mockUser);

        given(accountRepository.findByUserId(1L))
                .willReturn(List.of(account1));

        mockMvc.perform(get("/api/accounts")
                .with(user(mockUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].name").value("Cuenta 1"))
            .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void whenPutAccount_butBelongsToAnotherUser_thenForbidden() throws Exception {
        User anotherUser = new User();
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(anotherUser, 2L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Account accountOfAnotherUser = new Account();
        accountOfAnotherUser.setId(99L);
        accountOfAnotherUser.setName("Cuenta Ajena");
        accountOfAnotherUser.setUser(anotherUser);

        Account updatedDetails = new Account();
        updatedDetails.setName("Nombre Maligno");
        updatedDetails.setInitialBalance(BigDecimal.valueOf(1000));

        given(accountRepository.findById(99L)).willReturn(Optional.of(accountOfAnotherUser));

        mockMvc.perform(put("/api/accounts/99")
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
            .andExpect(status().isForbidden());
    }

    @Test
void whenDeleteAccount_butBelongsToAnotherUser_thenForbidden() throws Exception {
    User anotherUser = new User();
    try {
        java.lang.reflect.Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(anotherUser, 2L); 
    } catch (Exception e) {
        throw new RuntimeException(e);
    }

    Account accountOfAnotherUser = new Account();
    accountOfAnotherUser.setId(99L);
    accountOfAnotherUser.setName("Cuenta Ajena");
    accountOfAnotherUser.setUser(anotherUser);

    given(accountRepository.findById(99L)).willReturn(Optional.of(accountOfAnotherUser));
    org.mockito.Mockito.doNothing().when(accountRepository).delete(accountOfAnotherUser);


    mockMvc.perform(delete("/api/accounts/99")
            .with(user(mockUser)))
        .andExpect(status().isForbidden());
}
}
package com.myfinance.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
import com.myfinance.api.model.Subscription;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.SubscriptionRepository;
import com.myfinance.api.repository.UserRepository;
import com.myfinance.api.service.JwtService;

@WebMvcTest(SubscriptionController.class)
@Import(SecurityConfig.class)
public class SubscriptionControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private SubscriptionRepository subscriptionRepository;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private UserRepository userRepository;
    @MockBean private com.myfinance.api.repository.AccountRepository accountRepository; 
    private com.myfinance.api.model.Account mockAccount;

    @BeforeEach
    void setUp() throws Exception {
        mockUser = new User();
        setPrivateField(mockUser, "id", 1L);
        mockUser.setEmail("test@test.com");

        mockAccount = new com.myfinance.api.model.Account();
        setPrivateField(mockAccount, "id", 10L);
        mockAccount.setName("Banco");
        mockAccount.setUser(mockUser);
    }

    private User mockUser;


    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    void whenCreateSubscription_thenReturnsOk() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setName("Netflix");
        subscription.setAmount(new BigDecimal("15.99"));
        subscription.setRecurrence("MONTHLY");
        subscription.setNextPaymentDate(LocalDate.now().plusDays(5));
        
        subscription.setAccount(mockAccount);

        given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);

        mockMvc.perform(post("/api/subscriptions")
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subscription)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Netflix"));
    }

    @Test
    void whenGetSubscriptions_thenReturnsOnlyUserSubscriptions() throws Exception {
        Subscription sub1 = new Subscription();
        sub1.setName("Gym");
        sub1.setUser(mockUser);

        given(subscriptionRepository.findByUserId(1L)).willReturn(List.of(sub1));

        mockMvc.perform(get("/api/subscriptions")
                .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gym"));
        
        org.mockito.Mockito.verify(subscriptionRepository).findByUserId(1L);
    }
}
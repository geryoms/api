package com.myfinance.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.myfinance.api.repository.TransactionRepository;

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
import com.myfinance.api.model.Budget;
import com.myfinance.api.model.Category;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.BudgetRepository;
import com.myfinance.api.repository.CategoryRepository;
import com.myfinance.api.repository.UserRepository;
import com.myfinance.api.service.JwtService;

@WebMvcTest(BudgetController.class)
@Import(SecurityConfig.class)
public class BudgetControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private BudgetRepository budgetRepository;
    @MockBean private CategoryRepository categoryRepository;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private UserRepository userRepository;
    @MockBean private TransactionRepository transactionRepository;

    private User mockUser;
    private Category mockCategory;

    @BeforeEach
    void setUp() throws Exception {
        mockUser = new User();
        setPrivateField(mockUser, "id", 1L);
        mockUser.setEmail("test@test.com");

        mockCategory = new Category();
        setPrivateField(mockCategory, "id", 10L);
        mockCategory.setName("Comida");
        mockCategory.setUser(mockUser);
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    void whenCreateBudget_thenReturnsOk() throws Exception {
        Budget budget = new Budget();
        budget.setAmount(new BigDecimal("200.00"));
        budget.setStartDate(LocalDate.of(2023, 10, 1)); 
        budget.setCategory(mockCategory);

        given(categoryRepository.findById(10L)).willReturn(Optional.of(mockCategory));
        
        given(budgetRepository.findByUserIdAndCategoryIdAndStartDate(
                1L, 10L, LocalDate.of(2023, 10, 1)))
                .willReturn(Optional.empty());

        given(budgetRepository.save(any(Budget.class))).willReturn(budget);

        mockMvc.perform(post("/api/budgets")
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budget)))
                .andExpect(status().isOk());
    }

    @Test
    void whenCreateDuplicateBudget_thenReturnsConflict() throws Exception {
        Budget budget = new Budget();
        budget.setAmount(new BigDecimal("300.00"));
        budget.setStartDate(LocalDate.of(2023, 10, 1));
        budget.setCategory(mockCategory);

        given(categoryRepository.findById(10L)).willReturn(Optional.of(mockCategory));

        Budget existingBudget = new Budget();
        given(budgetRepository.findByUserIdAndCategoryIdAndStartDate(
                1L, 10L, LocalDate.of(2023, 10, 1)))
                .willReturn(Optional.of(existingBudget));

        mockMvc.perform(post("/api/budgets")
                .with(user(mockUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(budget)))
                .andExpect(status().isConflict());
    }
    @Test
void whenGetBudgetProgress_thenReturnsCalculatedValues() throws Exception {
    Budget budget = new Budget();
    setPrivateField(budget, "id", 1L);
    budget.setAmount(new BigDecimal("200.00"));
    budget.setStartDate(LocalDate.of(2023, 10, 1));
    budget.setCategory(mockCategory);
    budget.setUser(mockUser);

    given(budgetRepository.findById(1L)).willReturn(Optional.of(budget));
    given(transactionRepository.calculateTotalByCategoryAndDateRange(
            any(Long.class),     
            any(Long.class),     
            any(LocalDate.class), 
            any(LocalDate.class)  
    )).willReturn(new BigDecimal("50.00"));

    mockMvc.perform(get("/api/budgets/1/progress")
            .with(user(mockUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.limitAmount").value(200.0))
            .andExpect(jsonPath("$.spentAmount").value(50.0))
            .andExpect(jsonPath("$.remainingAmount").value(150.0));
}
}
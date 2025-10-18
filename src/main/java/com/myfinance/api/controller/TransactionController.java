package com.myfinance.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; // Importa ResponseEntity
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping; // Importa DeleteMapping
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Importa PathVariable
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping; // Importa PutMapping
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.myfinance.api.model.Category;
import com.myfinance.api.model.Transaction;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.CategoryRepository;
import com.myfinance.api.repository.TransactionRepository;

import org.slf4j.Logger; // Importa Logger
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // GET (Todas) - Ya lo tienes
    @GetMapping
public List<Transaction> getAllTransactions() {
    // 1. Obtiene el usuario que está autenticado a través del token
    User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    // 2. Usa el nuevo método del repositorio para buscar SOLAMENTE
    //    las transacciones de ese usuario, cargando sus categorías.
    return transactionRepository.findByUserId(currentUser.getId());
}

    // POST (Crear) - Ya lo tienes
   @PostMapping
    public Transaction createTransaction(@Valid @RequestBody Transaction transaction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        transaction.setUser(currentUser);

        // --- LÓGICA MEJORADA PARA LA CATEGORÍA ---
        if (transaction.getCategory() != null && transaction.getCategory().getId() != null) {
            // Busca la categoría completa en la base de datos
            Category category = categoryRepository.findById(transaction.getCategory().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            
            // Asegúrate de que la categoría pertenece al usuario actual (¡Bonus de seguridad!)
            if (category.getUser().getId() != currentUser.getId()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Category does not belong to the current user");
            }
            
            // Asigna la categoría completa (hidratada) a la transacción
            transaction.setCategory(category);
        }

        return transactionRepository.save(transaction);
    }

    // --- AÑADE ESTOS MÉTODOS ---

    // GET (Una por ID)
    // Busca una transacción por su ID. Si no la encuentra, devuelve un error 404 (Not Found).
   @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        log.info("User ID: {} is requesting transaction ID: {}", currentUser.getId(), id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Transaction with ID: {} not found.", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND);
                });

        // --- LÓGICA DE COMPARACIÓN MEJORADA Y LOGGING ---
        Long transactionUserId = transaction.getUser().getId();
        log.info("Transaction {} belongs to user ID: {}", id, transactionUserId);

        // Usamos .equals() para comparar objetos Long, es más seguro.
        if (!currentUser.getId().equals(transactionUserId)) {
            log.error("ACCESS DENIED: User ID {} tried to access transaction owned by user ID {}", currentUser.getId(), transactionUserId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
        }
        
        return ResponseEntity.ok(transaction);
    }

    // PUT (Actualizar por ID)
    // Actualiza una transacción existente.
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @Valid @RequestBody Transaction transactionDetails) {
        User currentUser = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Comprobamos que la transacción pertenece al usuario
        if (transaction.getUser().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        transaction.setDescription(transactionDetails.getDescription());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setDate(transactionDetails.getDate());
        transaction.setType(transactionDetails.getType());
        // (Aquí podrías añadir la lógica para actualizar la categoría también)

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    // DELETE (Borrar por ID)
    // Borra una transacción por su ID.
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Comprobamos que la transacción pertenece al usuario
        if (transaction.getUser().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        transactionRepository.delete(transaction);
        return ResponseEntity.ok().build();
    }

    // --- MÉTODO DE AYUDA ---
    // Para no repetir código, extraemos la lógica de obtener el usuario actual.
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
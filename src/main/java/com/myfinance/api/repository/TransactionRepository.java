package com.myfinance.api.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.myfinance.api.model.Transaction;

// JpaRepository nos da todos los métodos comunes: save, findById, findAll, delete...
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

/**
     * Busca todas las transacciones para un ID de usuario específico.
     * 'LEFT JOIN FETCH t.category' le dice a Hibernate que cargue la categoría
     * asociada en la misma consulta de base de datos, evitando problemas de
     * carga perezosa (Lazy Loading) y mejorando el rendimiento (evita el problema N+1).
     * @param userId El ID del usuario.
     * @return Una lista de transacciones con sus categorías ya cargadas.
     */
   @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category WHERE t.user.id = :userId")
List<Transaction> findByUserId(Long userId);

@Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.date BETWEEN :start AND :end")
    BigDecimal calculateTotalByTypeAndDateRange(Long userId, String type, LocalDate start, LocalDate end);

}
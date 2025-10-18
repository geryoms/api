package com.myfinance.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.myfinance.api.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Spring creará una consulta para buscar todas las categorías de un usuario por su ID
    List<Category> findByUserId(Long userId);
}
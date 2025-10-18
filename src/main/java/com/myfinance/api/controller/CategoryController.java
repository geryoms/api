package com.myfinance.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.myfinance.api.model.Category;
import com.myfinance.api.model.User;
import com.myfinance.api.repository.CategoryRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // GET (Listar) - Ya lo tenías
    @GetMapping
    public List<Category> getUserCategories() {
        User currentUser = getCurrentUser();
        return categoryRepository.findByUserId(currentUser.getId());
    }

    // POST (Crear) - Ya lo tenías
    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        User currentUser = getCurrentUser();
        category.setUser(currentUser);
        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    // --- AÑADE ESTOS MÉTODOS ---

    // PUT (Actualizar)
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category categoryDetails) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        // Comprobamos que la categoría pertenece al usuario
        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        category.setName(categoryDetails.getName());
        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    // DELETE (Borrar)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        // Comprobamos que la categoría pertenece al usuario
        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // OPCIONAL: Antes de borrar, podrías comprobar si alguna transacción usa esta categoría
        // y decidir qué hacer (impedir el borrado, poner la categoría de las transacciones a null, etc.)
        // Por ahora, la borraremos directamente.

        categoryRepository.delete(category);
        return ResponseEntity.noContent().build(); // 204 No Content es una respuesta estándar para un DELETE exitoso
    }

    // Método de ayuda para no repetir código
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
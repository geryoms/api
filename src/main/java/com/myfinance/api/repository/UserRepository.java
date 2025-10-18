package com.myfinance.api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.myfinance.api.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA creará automáticamente la consulta para buscar un usuario por su email.
    // Lo usaremos para el login.
    Optional<User> findByEmail(String email);
}
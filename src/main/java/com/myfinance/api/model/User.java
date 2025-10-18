package com.myfinance.api.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users") // Es buena práctica nombrar las tablas en plural
@Data
@NoArgsConstructor
public class User implements UserDetails { // Implementamos UserDetails para la seguridad

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true) // El email debe ser único
    @Email // Valida que el formato sea de email
    private String email;

    @NotBlank
    @Size(min = 8, max = 100) // La contraseña tendrá un hash, por eso el max es grande
    private String password;

    private String role = "USER"; // Por defecto, todos son usuarios normales

    // --- Métodos de la interfaz UserDetails ---
    // Spring Security los usará para gestionar la autenticación y autorización

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return this.email; // Usaremos el email como nombre de usuario para el login
    }

    // Estos métodos los dejamos en true por ahora
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
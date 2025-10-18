package com.myfinance.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.myfinance.api.repository.UserRepository;

@Configuration
public class ApplicationConfig {

    // BEAN 1: Le decimos a Spring cómo encontrar usuarios
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    // BEAN 2: Le decimos a Spring cómo encriptar contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // YA NO NECESITAMOS EL AUTHENTICATIONPROVIDER BEAN.
    // Spring lo construirá por nosotros en segundo plano usando los dos beans de arriba.
}
package com.myfinance.api.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity // Le dice a JPA que esta clase es una tabla en la BD
@Data   // De Lombok: crea getters, setters, toString(), etc. automáticamente
public class Transaction {

    @Id // Marca este campo como la clave primaria (ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // El ID se auto-genera
    private Long id;

    @NotBlank // No puede ser nulo ni estar vacío
    @Size(min = 3, max = 100) // Debe tener entre 3 y 100 caracteres
    private String description;

    @NotNull // No puede ser nulo
    @DecimalMin(value = "0.01") // El valor mínimo debe ser 0.01
    private BigDecimal amount;

    @NotNull
    private LocalDate date;

    @NotBlank
    private String type;

    @ManyToOne(fetch = FetchType.LAZY) // Muchas transacciones pueden pertenecer a Un usuario
    @JoinColumn(name = "user_id") // Nombre de la columna en la BD que guardará el ID del usuario
    @JsonIgnore // Evita que se incluya toda la info del usuario al devolver una transacción
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true) // nullable=true permite transacciones sin categoría
    private Category category;
}
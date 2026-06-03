package com.equivalentes.demo.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Llegada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private String tipo;

    private String fecha;

    private Double stock;

}
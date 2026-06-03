package com.equivalentes.demo.repository;


import com.equivalentes.demo.model.Llegada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LlegadaRepository
        extends JpaRepository<Llegada, Long> {

    List<Llegada> findByCodigoContainingIgnoreCase(
            String codigo


    );
}
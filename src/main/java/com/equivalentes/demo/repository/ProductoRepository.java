package com.equivalentes.demo.repository;

import com.equivalentes.demo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCodigoContainingIgnoreCaseOrEquivalenteContainingIgnoreCase(
            String codigo,
            String equivalente

    );
}
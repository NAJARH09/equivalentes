package com.equivalentes.demo.repository;

import com.equivalentes.demo.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRepository
        extends JpaRepository<Stock, Long> {

    List<Stock> findByCodigo(String codigo);
}
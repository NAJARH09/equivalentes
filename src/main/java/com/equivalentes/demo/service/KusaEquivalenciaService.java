package com.equivalentes.demo.service;


import com.equivalentes.demo.dto.EquivalenciaResultDTO;
import com.equivalentes.demo.model.Producto;
import com.equivalentes.demo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KusaEquivalenciaService {

    private final ProductoRepository repository;

    public List<EquivalenciaResultDTO> buscar(String codigo){

        List<Producto> productos =
                repository.findByCodigoContainingIgnoreCaseOrEquivalenteContainingIgnoreCase(
                        codigo,
                        codigo
                );

        List<EquivalenciaResultDTO> resultado =
                new ArrayList<>();

        for(Producto producto : productos){

            EquivalenciaResultDTO dto =
                    new EquivalenciaResultDTO();

            dto.setOrigen("KUSA");
            dto.setMarca("KUSA");
            dto.setCodigoEquivalente(
                    producto.getEquivalente()
            );
            dto.setDescripcion(
                    producto.getTipo()
            );

            resultado.add(dto);
        }

        return resultado;
    }

}
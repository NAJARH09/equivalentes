package com.equivalentes.demo.service;

import com.equivalentes.demo.model.Stock;
import com.equivalentes.demo.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class StockExcelService {

    @Autowired
    private StockRepository repository;

    public void importarStock(MultipartFile file) {

        try {

            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(
                                    file.getInputStream()
                            )
                    );

            repository.deleteAll();

            String linea;

            // LEER CABECERA
            String cabecera = br.readLine();

            String[] fechas =
                    cabecera.split(",");

            while ((linea = br.readLine()) != null) {

                String[] datos =
                        linea.split(",");

                // CODIGO
                String codigo =
                        datos[0].trim();

                // RECORRER FECHAS
                for (int i = 1;
                     i < datos.length;
                     i++) {

                    String fecha =
                            fechas[i];

                    String stockTexto =
                            datos[i].trim();

                    if (stockTexto.isBlank()) {
                        continue;
                    }

                    try {

                        Double stock =
                                Double.parseDouble(
                                        stockTexto
                                );

                        Stock s = new Stock();

                        s.setCodigo(codigo);

                        s.setFecha(fecha);

                        s.setStock(stock);

                        repository.save(s);

                    } catch (Exception e) {

                        System.out.println(
                                "ERROR STOCK"
                        );
                    }
                }
            }

            br.close();

            System.out.println(
                    "CSV IMPORTADO"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
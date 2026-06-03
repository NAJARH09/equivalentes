package com.equivalentes.demo.service;

import com.equivalentes.demo.model.Producto;
import com.equivalentes.demo.repository.ProductoRepository;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExcelService {

    @Autowired
    private ProductoRepository repository;

    public void importarExcel(MultipartFile file) {

        try {

            Workbook workbook =
                    new XSSFWorkbook(file.getInputStream());

            Sheet sheet = workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

            // BORRAR DATOS ANTERIORES
            repository.deleteAll();

            // RECORRER FILAS
            for (Row row : sheet) {

                // SALTAR CABECERA
                if (row.getRowNum() == 0) {
                    continue;
                }

                // COLUMNA A -> CODIGO
                String codigo =
                        formatter.formatCellValue(
                                row.getCell(0)
                        );

                // COLUMNA C -> DESCRIPCION
                String descripcion =
                        formatter.formatCellValue(
                                row.getCell(2)
                        );

                // COLUMNA D -> EQUIVALENTES
                String equivalentes =
                        formatter.formatCellValue(
                                row.getCell(3)
                        );

                // COLUMNA G -> TIPO
                String tipo =
                        formatter.formatCellValue(
                                row.getCell(6)
                        );

                // SI NO HAY CODIGO -> SALTAR
                if (codigo.isBlank()) {
                    continue;
                }

                // SEPARAR EQUIVALENTES
                String[] listaEquivalentes =
                        equivalentes.split("\\s+");

                // GUARDAR CADA EQUIVALENTE
                for (String eq : listaEquivalentes) {

                    if (!eq.isBlank()) {

                        Producto p = new Producto();

                        p.setCodigo(codigo);

                        p.setEquivalente(eq);

                        p.setTipo(tipo);


                        repository.save(p);
                    }
                }
            }

            workbook.close();

            System.out.println("EXCEL IMPORTADO");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
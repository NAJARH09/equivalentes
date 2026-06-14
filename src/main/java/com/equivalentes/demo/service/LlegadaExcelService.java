package com.equivalentes.demo.service;

import com.equivalentes.demo.model.Llegada;
import com.equivalentes.demo.repository.LlegadaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LlegadaExcelService {

    private final LlegadaRepository llegadaRepository;

    public void importar(MultipartFile file) {

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheet("EST_TOTAL_(2)");

            if (sheet == null) {
                throw new RuntimeException("No existe la hoja EST_TOTAL_(2)");
            }

            llegadaRepository.deleteAll();

            Row filaFechas = sheet.getRow(0);

            List<Llegada> lote = new ArrayList<>();

            for (int fila = 1; fila <= sheet.getLastRowNum(); fila++) {

                Row row = sheet.getRow(fila);

                if (row == null)
                    continue;

                String tipo = obtenerTexto(row.getCell(0));
                String codigo = obtenerTexto(row.getCell(1));

                if (codigo.isBlank())
                    continue;

                for (int col = 2; col < row.getLastCellNum(); col++) {

                    String fecha = obtenerTexto(filaFechas.getCell(col));

                    if (fecha.isBlank())
                        continue;

                    double stock = obtenerNumero(row.getCell(col));

                    if (stock > 0) {

                        Llegada llegada = new Llegada();

                        llegada.setTipo(tipo);
                        llegada.setCodigo(codigo);
                        llegada.setFecha(fecha);
                        llegada.setStock(stock);

                        lote.add(llegada);

                        if (lote.size() == 500) {
                            llegadaRepository.saveAll(lote);
                            lote.clear();
                        }
                    }
                }
            }

            if (!lote.isEmpty()) {
                llegadaRepository.saveAll(lote);
            }

            System.out.println("Llegadas importadas correctamente.");

        } catch (Exception e) {
            throw new RuntimeException("Error al importar el archivo Excel", e);
        }
    }

    private String obtenerTexto(Cell cell) {

        if (cell == null)
            return "";

        DataFormatter formatter = new DataFormatter();

        return formatter.formatCellValue(cell).trim();
    }

    private double obtenerNumero(Cell cell) {

        try {

            if (cell == null)
                return 0;

            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            }

            String valor = obtenerTexto(cell).replace(",", "");

            if (valor.isBlank())
                return 0;

            return Double.parseDouble(valor);

        } catch (Exception e) {
            return 0;
        }
    }
}
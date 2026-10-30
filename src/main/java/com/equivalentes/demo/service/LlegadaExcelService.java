
package com.equivalentes.demo.service;

import com.equivalentes.demo.model.Llegada;
import com.equivalentes.demo.repository.LlegadaRepository;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LlegadaExcelService {

    private final LlegadaRepository llegadaRepository;

    @Transactional
    public void importar(MultipartFile file) {

        // 1. Validar que se haya seleccionado un archivo.
        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Debes seleccionar un archivo Excel."
            );
        }

        try (Workbook workbook =
                     WorkbookFactory.create(file.getInputStream())) {

            // 2. Obtener la hoja exacta.
            Sheet sheet = workbook.getSheet("EST_TOTAL_(2)");

            if (sheet == null) {
                throw new RuntimeException(
                        "No existe la hoja EST_TOTAL_(2). "
                                + "Hojas disponibles: "
                                + obtenerHojas(workbook)
                );
            }

            DataFormatter formatter = new DataFormatter();

            // 3. Preparar los registros antes de borrar los anteriores.
            List<Llegada> llegadas = new ArrayList<>();

            // La fila 0 contiene los encabezados.
            for (int fila = 1;
                 fila <= sheet.getLastRowNum();
                 fila++) {

                Row row = sheet.getRow(fila);

                if (row == null) {
                    continue;
                }

                // Columna A: TIPO.
                String tipo = obtenerTexto(
                        row.getCell(0), formatter
                );

                // Columna B: CODIGO.
                String codigo = obtenerTexto(
                        row.getCell(1), formatter
                );

                // Columna D: FECHA.
                String fecha = obtenerFecha(
                        row.getCell(3), formatter
                );

                // Columna E: CANTIDAD.
                double cantidad = obtenerNumero(
                        row.getCell(4), formatter
                );

                // Ignorar filas sin los datos necesarios.
                if (codigo.isBlank()
                        || fecha.isBlank()
                        || cantidad <= 0) {
                    continue;
                }

                Llegada llegada = new Llegada();

                llegada.setTipo(tipo);
                llegada.setCodigo(codigo);
                llegada.setFecha(fecha);
                llegada.setStock(cantidad);

                llegadas.add(llegada);
            }

            // 4. No borrar datos si no se encontraron registros válidos.
            if (llegadas.isEmpty()) {
                throw new RuntimeException(
                        "El Excel no contiene llegadas válidas. "
                                + "No se reemplazaron los registros actuales."
                );
            }

            // 5. Reemplazar los registros anteriores.
            llegadaRepository.deleteAll();

            // 6. Guardar los registros del nuevo Excel.
            for (int inicio = 0;
                 inicio < llegadas.size();
                 inicio += 500) {

                int fin = Math.min(
                        inicio + 500,
                        llegadas.size()
                );

                List<Llegada> lote =
                        llegadas.subList(inicio, fin);

                llegadaRepository.saveAll(lote);
            }

            System.out.println(
                    "Llegadas importadas: " + llegadas.size()
            );

        } catch (Exception e) {

            // La excepción permite que @Transactional
            // revierta el borrado y las inserciones si fallan.
            throw new RuntimeException(
                    "Error al importar el Excel: " + e.getMessage(),
                    e
            );
        }
    }

    private String obtenerTexto(
            Cell cell, DataFormatter formatter) {

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }


    private String obtenerFecha(
            Cell cell, DataFormatter formatter) {

        if (cell == null) {
            return "";
        }

        // Caso 1: Excel reconoce la celda como fecha.
        if (cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {

            return new SimpleDateFormat("dd/MM/yyyy")
                    .format(cell.getDateCellValue());
        }

        // Caso 2: Excel guarda la fecha como número serial.
        if (cell.getCellType() == CellType.NUMERIC) {

            double valor = cell.getNumericCellValue();

            if (valor >= 20000 && valor <= 80000) {
                return new SimpleDateFormat("dd/MM/yyyy")
                        .format(DateUtil.getJavaDate(valor));
            }
        }

        // Caso 3: la fecha está guardada como texto.
        return obtenerTexto(cell, formatter);
    }


    private double obtenerNumero(
            Cell cell, DataFormatter formatter) {

        if (cell == null) {
            return 0;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            }

            String valor = obtenerTexto(cell, formatter);

            if (valor.isBlank()) {
                return 0;
            }

            // Ejemplo: "1,400" se convierte en 1400.
            valor = valor.replace(",", "");

            return Double.parseDouble(valor);

        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String obtenerHojas(Workbook workbook) {

        StringBuilder hojas = new StringBuilder();

        for (int i = 0;
             i < workbook.getNumberOfSheets();
             i++) {

            if (i > 0) {
                hojas.append(", ");
            }

            hojas.append(workbook.getSheetName(i));
        }

        return hojas.toString();
    }
}

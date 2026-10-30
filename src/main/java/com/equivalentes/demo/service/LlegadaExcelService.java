
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

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Debes seleccionar un archivo Excel."
            );
        }

        try (Workbook workbook =
                     WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheet("EST_TOTAL_(2)");

            if (sheet == null) {
                throw new RuntimeException(
                        "No existe la hoja EST_TOTAL_(2). Hojas: "
                                + obtenerHojas(workbook)
                );
            }

            DataFormatter formatter = new DataFormatter();

            // Buscar las columnas mediante sus encabezados.
            int filaEncabezado = -1;
            int colTipo = -1;
            int colCodigo = -1;
            int colFecha = -1;
            int colCantidad = -1;

            for (int i = 0;
                 i <= Math.min(sheet.getLastRowNum(), 10);
                 i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                int tipo = -1;
                int codigo = -1;
                int fecha = -1;
                int cantidad = -1;

                for (Cell cell : row) {

                    String encabezado = formatter
                            .formatCellValue(cell)
                            .trim()
                            .toUpperCase();

                    switch (encabezado) {
                        case "TIPO" -> tipo = cell.getColumnIndex();
                        case "CODIGO" -> codigo = cell.getColumnIndex();
                        case "FECHA" -> fecha = cell.getColumnIndex();
                        case "CANTIDAD" -> cantidad = cell.getColumnIndex();
                    }
                }

                if (codigo >= 0 && fecha >= 0 && cantidad >= 0) {
                    filaEncabezado = i;
                    colTipo = tipo;
                    colCodigo = codigo;
                    colFecha = fecha;
                    colCantidad = cantidad;
                    break;
                }
            }

            if (filaEncabezado < 0) {
                throw new RuntimeException(
                        "No se encontraron los encabezados CODIGO, "
                                + "FECHA y CANTIDAD."
                );
            }

            List<Llegada> llegadas = new ArrayList<>();

            // Leer las filas que están debajo de los encabezados.
            for (int i = filaEncabezado + 1;
                 i <= sheet.getLastRowNum();
                 i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                String tipo = colTipo >= 0
                        ? obtenerTexto(row.getCell(colTipo), formatter)
                        : "";

                String codigo = obtenerTexto(
                        row.getCell(colCodigo), formatter
                );

                String fecha = obtenerFecha(
                        row.getCell(colFecha), formatter
                );

                double cantidad = obtenerNumero(
                        row.getCell(colCantidad), formatter
                );

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

            // No borrar los datos anteriores si la lectura falla.
            if (llegadas.isEmpty()) {
                throw new RuntimeException(
                        "No se encontraron llegadas válidas. "
                                + "Se conservaron los registros anteriores."
                );
            }

            // Reemplazar los registros anteriores.
            llegadaRepository.deleteAll();
            llegadaRepository.saveAll(llegadas);

            System.out.println(
                    "Total de llegadas importadas: " + llegadas.size()
            );

        } catch (Exception e) {
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

        if (cell.getCellType() == CellType.NUMERIC) {

            double valor = cell.getNumericCellValue();

            if (DateUtil.isCellDateFormatted(cell)
                    || (valor >= 20000 && valor <= 80000)) {

                return new SimpleDateFormat("dd/MM/yyyy")
                        .format(DateUtil.getJavaDate(valor));
            }
        }

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

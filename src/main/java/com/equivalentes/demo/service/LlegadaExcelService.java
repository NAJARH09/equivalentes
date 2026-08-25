package com.equivalentes.demo.service;

import com.equivalentes.demo.model.Llegada;
import com.equivalentes.demo.repository.LlegadaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
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
                throw new RuntimeException(
                        "No existe la hoja EST_TOTAL_(2). Hojas disponibles: "
                                + obtenerHojas(workbook)
                );
            }

            // Borramos las llegadas anteriores
            llegadaRepository.deleteAll();

            List<Llegada> lote = new ArrayList<>();

            /*
             * Estructura real del Excel:
             *
             * B = CODIGO       -> índice 1
             * C = DESC         -> índice 2
             * D = FECHA_LLEGADA -> índice 3
             * E = CANTIDAD     -> índice 4
             */

            for (int fila = 1; fila <= sheet.getLastRowNum(); fila++) {

                Row row = sheet.getRow(fila);

                if (row == null) {
                    continue;
                }

                // B = CODIGO
                String codigo = obtenerTexto(row.getCell(1));

                if (codigo.isBlank()) {
                    continue;
                }

                // D = FECHA_LLEGADA
                String fecha = obtenerFecha(row.getCell(3));

                if (fecha.isBlank()) {
                    continue;
                }

                // E = CANTIDAD
                double cantidad = obtenerNumero(row.getCell(4));

                // Si la cantidad es 0, no guardamos
                if (cantidad <= 0) {
                    continue;
                }

                Llegada llegada = new Llegada();

                llegada.setCodigo(codigo);
                llegada.setFecha(fecha);
                llegada.setStock(cantidad);

                /*
                 * Si tu modelo Llegada todavía tiene "tipo",
                 * puedes dejarlo vacío porque este Excel ya no tiene
                 * una columna TIPO.
                 */
                llegada.setTipo("");

                lote.add(llegada);

                // Guardamos cada 500 registros
                if (lote.size() == 500) {

                    llegadaRepository.saveAll(lote);

                    lote.clear();
                }
            }

            // Guardar los registros restantes
            if (!lote.isEmpty()) {

                llegadaRepository.saveAll(lote);
            }

            System.out.println("=================================");
            System.out.println("LLEGADAS IMPORTADAS CORRECTAMENTE");
            System.out.println("=================================");

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Error al importar el archivo Excel: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Obtiene texto de una celda.
     */
    private String obtenerTexto(Cell cell) {

        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();

        return formatter.formatCellValue(cell).trim();
    }

    /**
     * Obtiene una fecha de Excel.
     *
     * Si la celda es una fecha real de Excel,
     * la convierte a dd/MM/yyyy.
     */
    private String obtenerFecha(Cell cell) {

        if (cell == null) {
            return "";
        }

        try {

            if (cell.getCellType() == CellType.NUMERIC
                    && DateUtil.isCellDateFormatted(cell)) {

                SimpleDateFormat formato =
                        new SimpleDateFormat("dd/MM/yyyy");

                return formato.format(cell.getDateCellValue());
            }

            return obtenerTexto(cell);

        } catch (Exception e) {

            return "";
        }
    }

    /**
     * Obtiene números de Excel.
     */
    private double obtenerNumero(Cell cell) {

        if (cell == null) {
            return 0;
        }

        try {

            if (cell.getCellType() == CellType.NUMERIC) {

                return cell.getNumericCellValue();
            }

            String valor = obtenerTexto(cell);

            if (valor.isBlank()) {
                return 0;
            }

            // Por si Excel trae 1,920
            valor = valor.replace(",", "");

            return Double.parseDouble(valor);

        } catch (Exception e) {

            return 0;
        }
    }

    /**
     * Muestra las hojas disponibles en caso de error.
     */
    private String obtenerHojas(Workbook workbook) {

        StringBuilder hojas = new StringBuilder();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

            if (i > 0) {
                hojas.append(", ");
            }

            hojas.append(workbook.getSheetName(i));
        }

        return hojas.toString();
    }
}
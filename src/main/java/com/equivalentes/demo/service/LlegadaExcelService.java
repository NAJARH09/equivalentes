package com.equivalentes.demo.service;

import com.equivalentes.demo.model.Llegada;
import com.equivalentes.demo.repository.LlegadaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LlegadaExcelService {

    private final LlegadaRepository llegadaRepository;

    public void importar(MultipartFile file) {

        try {

            Workbook workbook =
                    WorkbookFactory.create(
                            file.getInputStream()
                    );
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

                System.out.println(
                        workbook.getSheetAt(i).getSheetName()
                );
            }

            Sheet sheet = workbook.getSheet("EST_TOTAL_(2)");
            for (int i = 0; i < 5; i++) {

                Row row = sheet.getRow(i);

                if(row == null){
                    System.out.println("FILA " + i + " ES NULL");
                    continue;
                }

                System.out.println("\n===== FILA " + i + " =====");

                for(int j = 0; j < row.getLastCellNum(); j++){

                    System.out.print(
                            "[" +
                                    new DataFormatter()
                                            .formatCellValue(row.getCell(j))
                                    + "]"
                    );
                }

                System.out.println();

            }
            llegadaRepository.deleteAll();

            Row filaFechas = sheet.getRow(0);

            for(int fila = 1;
                fila <= sheet.getLastRowNum();
                fila++) {

                Row row = sheet.getRow(fila);

                if(row == null)
                    continue;

                String tipo =
                        obtenerTexto(
                                row.getCell(0)
                        );

                String codigo =
                        obtenerTexto(
                                row.getCell(1)
                        );

                if(codigo.isBlank())
                    continue;

                for(int col = 2;
                    col < row.getLastCellNum();
                    col++) {

                    String fecha =
                            obtenerTexto(
                                    filaFechas.getCell(col)
                            );

                    if(fecha.isBlank())
                        continue;

                    double stock =
                            obtenerNumero(
                                    row.getCell(col)
                            );

                    if(stock > 0){

                        Llegada llegada =
                                new Llegada();

                        llegada.setTipo(tipo);
                        llegada.setCodigo(codigo);
                        llegada.setFecha(fecha);
                        llegada.setStock(stock);

                        System.out.println(
                                "CODIGO = " + codigo +
                                        " | FECHA = " + fecha +
                                        " | STOCK = " + stock
                        );

                        llegadaRepository.save(
                                llegada
                        );
                    }
                }
            }

            workbook.close();

            System.out.println(
                    "Llegadas importadas correctamente"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    private String obtenerTexto(Cell cell){

        if(cell == null)
            return "";

        DataFormatter formatter =
                new DataFormatter();

        return formatter
                .formatCellValue(cell)
                .trim();
    }

    private double obtenerNumero(Cell cell){

        try{

            if(cell == null)
                return 0;

            if(cell.getCellType()
                    == CellType.NUMERIC){

                return cell.getNumericCellValue();
            }

            String valor =
                    obtenerTexto(cell)
                            .replace(",", "");

            if(valor.isBlank())
                return 0;

            return Double.parseDouble(valor);

        }catch(Exception e){

            return 0;
        }
    }
}
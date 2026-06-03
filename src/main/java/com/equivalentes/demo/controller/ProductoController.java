package com.equivalentes.demo.controller;



import com.equivalentes.demo.model.Llegada;
import com.equivalentes.demo.repository.LlegadaRepository;
import com.equivalentes.demo.service.ExcelService;
import com.equivalentes.demo.service.LlegadaExcelService;
import com.equivalentes.demo.service.StockExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import com.equivalentes.demo.model.Producto;
import com.equivalentes.demo.repository.ProductoRepository;
import com.equivalentes.demo.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.ArrayList;
@Controller
public class ProductoController {
    @GetMapping("/autocomplete")
    @ResponseBody
    public List<String> autocomplete(
            @RequestParam String term) {

        return repository
                .findByCodigoContainingIgnoreCaseOrEquivalenteContainingIgnoreCase(
                        term,
                        term
                )
                .stream()
                .limit(10)
                .map(Producto::getCodigo)
                .distinct()
                .toList();
    }

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ProductoRepository repository;

    @Autowired
    private StockExcelService stockExcelService;

    @Autowired
    private LlegadaExcelService llegadaExcelService;
    @Autowired
    private LlegadaRepository llegadaRepository;

    @GetMapping("/")
    public String inicio(
            @RequestParam(required = false)
            String buscar,
            Model model) {

        List<Producto> productos;

        if (buscar != null && !buscar.isBlank()) {

            productos =
                    repository
                            .findByCodigoContainingIgnoreCaseOrEquivalenteContainingIgnoreCase(
                                    buscar,
                                    buscar
                            );

        } else {

            productos = repository.findAll();
        }

        List<Llegada> stocks;

        if (buscar != null && !buscar.isBlank()) {

            stocks =
                    llegadaRepository
                            .findByCodigoContainingIgnoreCase(
                                    buscar
                            );

        } else {

            stocks = llegadaRepository.findAll();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("stocks", stocks);
        model.addAttribute("buscar", buscar);

        return "index";
    }

    @PostMapping("/importar")
    public String importarExcel(
            @RequestParam("file")
            MultipartFile file) {

        excelService.importarExcel(file);

        return "redirect:/";
    }
    @PostMapping("/importar-stock")
    public String importarStock(
            @RequestParam("file")
            MultipartFile file) {

        stockExcelService.importarStock(file);

        return "redirect:/";
    }
    @PostMapping("/importar-llegadas")
    public String importarLlegadas(
            @RequestParam("file") MultipartFile file){

        llegadaExcelService.importar(file);


        return "redirect:/";
    }
}
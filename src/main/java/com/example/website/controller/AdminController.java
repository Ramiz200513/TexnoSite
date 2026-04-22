package com.example.website.controller;

import com.example.website.models.Product;
import com.example.website.models.WebUser;
import com.example.website.repository.OrderRepository;
import com.example.website.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final com.example.website.service.ExcelService excelService;
    // Один конструктор для всех репозиториев
    public AdminController(ProductRepository productRepository,
                           OrderRepository orderRepository,
                           com.example.website.service.ExcelService excelService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.excelService = excelService;
    }

    @GetMapping
    public String adminPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "id_desc") String sort, // Сортировка по умолчанию
            Model model, HttpSession session) {

        // Проверка прав (админ)
        WebUser user = (WebUser) session.getAttribute("user");
        if (user == null || !user.getUsername().equals("admin")) return "redirect:/products";

        // Логика сортировки
        Sort sortObj = switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name_asc" -> Sort.by("name").ascending();
            default -> Sort.by("id").descending();
        };

        // Получаем данные с учетом всех фильтров и сортировки
        var productPage = productRepository.findFiltered(
                keyword, category, minPrice, maxPrice, PageRequest.of(0, 1000, sortObj)
        );

        // Отправляем всё в модель для отображения в форме
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("categories", productRepository.findAllCategories());
        model.addAttribute("orders", orderRepository.findAll());
        model.addAttribute("newProduct", new Product());

        // Сохраняем значения фильтров, чтобы они не сбрасывались в UI
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCat", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currentSort", sort);

        return "admin";
    }

    @GetMapping("/export/excel")
    public org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody exportToExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "id_desc") String sort,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {

//        Sort sortObj = sort.equals("price_asc") ? Sort.by("price").ascending() :
//                sort.equals("price_desc") ? Sort.by("price").descending() : Sort.by("id").descending();

        Sort sortObj = switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            default -> Sort.by("id").descending();
        };

        List<Product> products = productRepository.findFiltered(
                keyword, category, minPrice, maxPrice, PageRequest.of(0, 1000, sortObj)
        ).getContent();

        ByteArrayInputStream in = excelService.productsToExcel(products);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=report.xlsx");

        return outputStream -> in.transferTo(outputStream);
    }
    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product,
                             @RequestParam(value = "imageFile", required = false) MultipartFile file) throws IOException {

        if (file != null && !file.isEmpty()) {
            String uploadDir = "./uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            product.setImageUrl("/uploads/" + fileName);
        } else if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
            product.setImageUrl("https://placehold.co/400x400?text=No+Image");
        }

        productRepository.save(product);
        return "redirect:/admin";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id) {
        productRepository.deleteById(id);
        return "redirect:/admin";
    }
}
package com.example.website.controller;

import com.example.website.models.Product;
import com.example.website.models.WebUser;
import com.example.website.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
public class AdminRestController {

    private final ProductRepository productRepository;

    public AdminRestController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam(value = "imageFile", required = false) MultipartFile file,
            HttpSession session) throws IOException {

        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setCategory(category);
        product.setDescription(description);

        if (file != null && !file.isEmpty()) {
            String uploadDir = "./uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            product.setImageUrl("/uploads/" + fileName);
        } else {
            product.setImageUrl("https://placehold.co/400x400?text=No+Image");
        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Нельзя удалить товар: он уже находится в оформленных заказах покупателей.");
        }
    }

    private boolean isAdmin(HttpSession session) {
        WebUser user = (WebUser) session.getAttribute("user");
        return user != null && "admin".equals(user.getUsername());
    }
}
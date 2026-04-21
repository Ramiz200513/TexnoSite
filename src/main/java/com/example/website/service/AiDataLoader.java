package com.example.website.service;

import com.example.website.models.Product;
import com.example.website.repository.ProductRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AiDataLoader implements CommandLineRunner {

    private final VectorStore vectorStore;
    private final ProductRepository productRepository;

    public AiDataLoader(VectorStore vectorStore, ProductRepository productRepository) {
        this.vectorStore = vectorStore;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Начинаем загрузку товаров в ИИ Ollama...");

        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            System.out.println("База товаров пуста.");
            return;
        }

        List<Document> documents = products.stream().map(product -> {
            String text = String.format("Название: %s. Категория: %s. Цена: %s$. Описание: %s",
                    product.getName(),
                    product.getCategory(),
                    product.getPrice(),
                    product.getDescription());
            return new Document(text, Map.of("productId", product.getId()));
        }).collect(Collectors.toList());

        vectorStore.add(documents);
        System.out.println("Готово! Ollama выучила " + documents.size() + " товаров.");
    }
}
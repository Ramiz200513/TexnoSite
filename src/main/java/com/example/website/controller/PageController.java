package com.example.website.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    // Эта ссылка откроет нашу новую страницу сканера
    @GetMapping("/ocr")
    public String ocrPage() {
        return "ocr"; // Ищет файл ocr.html в папке templates
    }
}
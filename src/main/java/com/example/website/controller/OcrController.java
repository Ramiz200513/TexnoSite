package com.example.website.controller;

import com.example.website.service.OcrService;
import org.springframework.ai.chat.ChatClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    private final OcrService ocrService;
    private final ChatClient chatClient;

    public OcrController(OcrService ocrService, ChatClient chatClient) {
        this.ocrService = ocrService;
        this.chatClient = chatClient;
    }

    @PostMapping("/process")
    public OcrResult processImage(@RequestParam("file") MultipartFile file) throws Exception {
        File tempFile = File.createTempFile("ocr_", file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        String originalText = ocrService.extractText(tempFile);

        String prompt = " 2. Перевести итоговый смысл на русский язык.Переводи только текст не добавляй новых слов и так далее. Нужен только перевод " + originalText;
        String translatedText = chatClient.call(prompt);

        return new OcrResult(originalText, translatedText);
    }
    public record OcrResult(String original, String translated) {}
}
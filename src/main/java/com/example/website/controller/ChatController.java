package com.example.website.controller;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatController(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String message) {
        // 1. Ищем похожие товары
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(message));
        String content = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        // 2. Формируем контекст для Ollama
        String systemText = """
    ТЫ — БОТ-АВТООТВЕТЧИК. ТВОЯ ЗАДАЧА СТРОГО ПЕРЕСКАЗАТЬ ДАННЫЕ.
    
    ПРАВИЛА:
    1. Если категория 'Телефоны', ЗАПРЕЩЕНО использовать слово 'ноутбук'.
    2. Если категория 'Игры', ЗАПРЕЩЕНО использовать слово 'приставка', если его нет в тексте.
    3. Пиши ответ СТРОГО по шаблону: 
       [Название] — [Цена]. Описание: [Описание из базы].
    4. Если информации нет, просто скажи: 'Товар не найден'.
    5. НЕ ДОБАВЛЯЙ ОТ СЕБЯ НИ ОДНОГО СЛОВА про характеристики.

    ДАННЫЕ ИЗ БАЗЫ:
    %s
    """.formatted(content);
        SystemMessage systemMessage = new SystemMessage(systemText);
        UserMessage userMessage = new UserMessage(message);

        // 3. Отправляем запрос и ждем ответ
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
}
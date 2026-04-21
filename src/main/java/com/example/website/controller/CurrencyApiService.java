package com.example.website.controller;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CurrencyApiService {

    private final RestClient restClient;

    public CurrencyApiService() {
        this.restClient = RestClient.create();
    }

    public Double getActualKztRate() {
        try {
            ExchangeRateResponse response = restClient.get()
                    .uri("https://open.er-api.com/v6/latest/USD")
                    .retrieve()
                    .body(ExchangeRateResponse.class);

            if (response != null && response.getRates() != null) {
                return response.getRates().getOrDefault("KZT", 450.0);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении курса валют: " + e.getMessage());
        }
        return 450.0;
    }
}
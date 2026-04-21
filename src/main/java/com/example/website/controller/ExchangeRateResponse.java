package com.example.website.controller;

import java.util.Map;


public class ExchangeRateResponse {
    private String base_code;
    private Map<String, Double> rates;

    public String getBase_code() { return base_code; }
    public void setBase_code(String base_code) { this.base_code = base_code; }

    public Map<String, Double> getRates() { return rates; }
    public void setRates(Map<String, Double> rates) { this.rates = rates; }
}
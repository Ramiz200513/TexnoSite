package com.example.website.models;

import jakarta.persistence.*;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "ProductID")
    private Product product;

    private Integer quantity = 1;
    private Double priceAtPurchase; // Цена на момент покупки

    // Геттеры и сеттеры
    public void setOrder(Order order) { this.order = order; }
    public void setProduct(Product product) { this.product = product; }
    public void setPriceAtPurchase(Double price) { this.priceAtPurchase = price; }
}
package com.example.website.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderID") // Это важно!
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "UserID") // Имя колонки в базе
    private WebUser user;
    private LocalDateTime orderDate = LocalDateTime.now();
    private Double totalAmount;
    private String status = "Новый"; // Новый, Оплачен, Доставлен

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public WebUser getUser() { return user; }
    public void setUser(WebUser user) { this.user = user; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
}
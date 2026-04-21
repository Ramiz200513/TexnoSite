package com.example.website.models;

import jakarta.persistence.*;

@Entity
@Table(name = "WebProducts")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    private Integer id;

    private String name;
    private Double price;
    private String description;
    private String imageUrl;
    private String category;
    private Integer rating;

    public Product() {}

    // Стандартные геттеры и сеттеры без условий
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
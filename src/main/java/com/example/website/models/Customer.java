package com.example.website.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Customer", schema = "Sales")
public class Customer {
    @Id
    @Column(name = "CustomerID")
    private Integer id;


    @Column(name = "AccountNumber")
    private String accountNumber;

    public Integer getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
}
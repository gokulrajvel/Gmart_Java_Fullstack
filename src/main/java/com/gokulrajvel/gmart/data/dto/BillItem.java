package com.gokulrajvel.gmart.data.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "bill_items")
public class BillItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @Column(name = "product_id")
    private int productId;

    @Column(name = "product_name")
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "price_at_sale", nullable = false)
    private double priceAtSale;

    public BillItem() {}

    public BillItem(int productId, int quantity, double priceAtSale) {
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtSale = priceAtSale;
    }

    public BillItem(int productId, String productName, int quantity, double priceAtSale) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.priceAtSale = priceAtSale;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }


    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPriceAtSale() { return priceAtSale; }
    public void setPriceAtSale(double priceAtSale) { this.priceAtSale = priceAtSale; }
}

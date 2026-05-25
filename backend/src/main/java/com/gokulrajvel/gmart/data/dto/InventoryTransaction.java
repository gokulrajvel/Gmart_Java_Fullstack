package com.gokulrajvel.gmart.data.dto;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transactions")
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "product_id")
    private int productId;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // INWARD or OUTWARD

    @Column(nullable = false)
    private int quantity;

    @Column(name = "transaction_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate = new Date();

    public InventoryTransaction() {}

    public InventoryTransaction(int productId, int userId, String transactionType, int quantity) {
        this.productId = productId;
        this.userId = userId;
        this.transactionType = transactionType;
        this.quantity = quantity;
    }

    public InventoryTransaction(int id, int productId, int userId, String transactionType, int quantity, Date transactionDate) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.transactionDate = transactionDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Date getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Date transactionDate) { this.transactionDate = transactionDate; }
}

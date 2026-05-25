package com.gokulrajvel.gmart.data.dto;

import com.gokulrajvel.gmart.data.PaymentMethod;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "tax_amount", nullable = false)
    private double taxAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "bill_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date billDate = new Date();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private List<BillItem> items;

    public Bill() {}

    public Bill(int userId, double totalAmount, double taxAmount, String paymentMethod) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.taxAmount = taxAmount;
        this.paymentMethod = paymentMethod != null ? PaymentMethod.valueOf(paymentMethod.trim().toUpperCase()) : null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public String getPaymentMethod() { return paymentMethod != null ? paymentMethod.name() : null; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod != null ? PaymentMethod.valueOf(paymentMethod.trim().toUpperCase()) : null; }

    public Date getBillDate() { return billDate; }
    public void setBillDate(Date billDate) { this.billDate = billDate; }

    public List<BillItem> getItems() { return items; }
    public void setItems(List<BillItem> items) { this.items = items; }
    public void setBillItems(List<BillItem> items) { this.items = items; }
}

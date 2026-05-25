package com.gokulrajvel.gmart.features.billing;

import com.gokulrajvel.gmart.data.dto.*;
import com.gokulrajvel.gmart.data.repository.GmartDB;
import java.util.ArrayList;
import java.util.List;

public class BillingModel {
    private BillingPresenter presenter;
    private List<OrderItem> cart;
    private static final double TAX_RATE = 0.05;

    public BillingModel(BillingPresenter presenter) {
        this.presenter = presenter;
        this.cart = new ArrayList<>();
    }

    public Product getProductBySku(String sku) {
        return GmartDB.getInstance().getProductBySku(sku);
    }

    public boolean addToCart(Product product, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        int currentQuantityInCart = 0;
        OrderItem existingItem = null;
        for (OrderItem item : cart) {
            if (item.getProduct().getId() == product.getId()) {
                currentQuantityInCart += item.getQuantity();
                existingItem = item;
                break;
            }
        }
        
        if (currentQuantityInCart + quantity <= product.getStockQuantity()) {
            if (existingItem != null) {
                existingItem.quantity += quantity;
            } else {
                cart.add(new OrderItem(product, quantity));
            }
            return true;
        }
        return false;
    }

    public List<OrderItem> getCart() {
        return cart;
    }

    public void clearCart() {
        cart.clear();
    }

    public double calculateSubtotal() {
        double subtotal = 0;
        for (OrderItem item : cart) {
            subtotal += item.getProduct().getPrice() * item.getQuantity();
        }
        return subtotal;
    }

    public double calculateTax() {
        return calculateSubtotal() * TAX_RATE;
    }

    public double calculateTotal() {
        return calculateSubtotal() + calculateTax();
    }

    public void finalizeSale(int userId, String paymentMethod) {
        double total = calculateTotal();
        double tax = calculateTax();

        Bill bill = new Bill(userId, total, tax, paymentMethod);
        List<BillItem> billItems = new ArrayList<>();

        for (OrderItem item : cart) {
            // Inventory Update
            GmartDB.getInstance().updateProductStock(item.getProduct().getId(), -item.getQuantity());
            
            // Record Inventory Transaction
            InventoryTransaction transaction = new InventoryTransaction(
                item.getProduct().getId(),
                userId,
                "OUTWARD",
                item.getQuantity()
            );
            GmartDB.getInstance().recordTransaction(transaction);

            // Prepare Bill Line Item
            billItems.add(new BillItem(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getProduct().getPrice()
            ));
        }

        // Save formal Bill record
        GmartDB.getInstance().createBill(bill, billItems);
        
        clearCart();
    }

    public static class OrderItem {
        Product product;
        int quantity;

        OrderItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
    }
}

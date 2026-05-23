package com.gokulrajvel.gmart.features.inventorytracking;

import com.gokulrajvel.gmart.data.dto.Product;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.util.ConsoleInput;
import java.util.List;

public class InventoryView {
    private InventoryPresenter presenter;
    private User currentUser;

    public InventoryView() {
        this.presenter = new InventoryPresenter(this);
    }

    public InventoryView(User user) {
        this.currentUser = user;
        this.presenter = new InventoryPresenter(this);
    }

    public void display() {
        System.out.println("\n--- Inventory Tracking ---");
        List<Product> products = presenter.getProducts();
        
        if (products.isEmpty()) {
            System.out.println("No products found in inventory.");
        } else {
            if (currentUser != null && currentUser.getRole() == Role.BILLING_STAFF) {
                System.out.printf("%-15s %-20s %-10s\n", "SKU", "Name", "Amount");
                System.out.println("----------------------------------------------");
                for (Product p : products) {
                    System.out.printf("%-15s %-20s %-10.2f\n", 
                        p.getSkuCode(), p.getName(), p.getPrice());
                }
            } else {
                System.out.printf("%-10s %-15s %-20s %-10s %-10s\n", "ID", "SKU", "Name", "Price", "Stock");
                System.out.println("------------------------------------------------------------------");
                for (Product p : products) {
                    System.out.printf("%-10d %-15s %-20s %-10.2f %-10d\n", 
                        p.getId(), p.getSkuCode(), p.getName(), p.getPrice(), p.getStockQuantity());
                }
            }
        }
        
        System.out.println("\nPress Enter to return to Dashboard...");
        ConsoleInput.getScanner().nextLine();
    }
}

package com.gokulrajvel.gmart.features.stock;

import com.gokulrajvel.gmart.data.dto.Supplier;
import com.gokulrajvel.gmart.util.ConsoleInput;
import java.util.List;

public class StockView {
    private StockPresenter presenter;

    public StockView() {
        this.presenter = new StockPresenter(this);
    }

    public void display() {
        while (true) {
            System.out.println("\n--- Stock Management ---");
            System.out.println("1. Add Category");
            System.out.println("2. Add Product");
            System.out.println("0. Back to Dashboard");
            System.out.print("Enter choice: ");

            int choice = ConsoleInput.readInt();

            switch (choice) {
                case 1: addCategory(); break;
                case 2: addProduct(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void addCategory() {
        System.out.print("Enter Category Name: ");
        String name = ConsoleInput.getScanner().nextLine();
        presenter.addCategory(name);
    }

    private void addProduct() {
        System.out.print("Enter SKU Code: ");
        String sku = ConsoleInput.getScanner().nextLine();
        System.out.print("Enter Product Name: ");
        String name = ConsoleInput.getScanner().nextLine();
        
        System.out.println("Select Category ID (Manual entry for now):");
        int catId = ConsoleInput.readInt();
        
        System.out.println("Select Supplier ID (Manual entry for now):");
        int suppId = ConsoleInput.readInt();
        
        System.out.print("Enter Price: ");
        double price = Double.parseDouble(ConsoleInput.getScanner().nextLine());
        System.out.print("Enter Initial Stock: ");
        int stock = ConsoleInput.readInt();

        presenter.addProduct(sku, name, catId, suppId, price, stock);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}

package com.gokulrajvel.gmart.features.supplier;

import com.gokulrajvel.gmart.data.dto.Supplier;
import com.gokulrajvel.gmart.util.ConsoleInput;
import java.util.List;

public class SupplierView {
    private SupplierPresenter presenter;

    public SupplierView() {
        this.presenter = new SupplierPresenter(this);
    }

    public void display() {
        while (true) {
            System.out.println("\n--- Supplier Management ---");
            System.out.println("1. Add Supplier");
            System.out.println("2. View All Suppliers");
            System.out.println("0. Back to Dashboard");
            System.out.print("Enter choice: ");

            int choice = ConsoleInput.readInt();

            switch (choice) {
                case 1:
                    addSupplier();
                    break;
                case 2:
                    viewSuppliers();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void addSupplier() {
        System.out.print("Enter Supplier Name: ");
        String name = ConsoleInput.getScanner().nextLine();
        System.out.print("Enter Contact Info: ");
        String contact = ConsoleInput.getScanner().nextLine();
        presenter.addSupplier(name, contact);
    }

    private void viewSuppliers() {
        List<Supplier> suppliers = presenter.getSuppliers();
        if (suppliers.isEmpty()) {
            System.out.println("No suppliers found.");
        } else {
            System.out.printf("%-10s %-20s %-30s\n", "ID", "Name", "Contact");
            System.out.println("------------------------------------------------------------");
            for (Supplier s : suppliers) {
                System.out.printf("%-10d %-20s %-30s\n", s.getId(), s.getName(), s.getContactInfo());
            }
        }
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}

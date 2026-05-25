package com.gokulrajvel.gmart.features.inwardandoutward;

import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.util.ConsoleInput;

public class InwardandOutwardView {
    private InwardandOutwardPresenter presenter;
    private User currentUser;

    public InwardandOutwardView(User user) {
        this.currentUser = user;
        this.presenter = new InwardandOutwardPresenter(this);
    }

    public void display() {
        while (true) {
            System.out.println("\n--- Inward and Outward Transactions ---");
            System.out.println("1. Record Inward (Stock In)");
            System.out.println("2. Record Outward (Stock Out)");
            System.out.println("0. Back to Dashboard");
            System.out.print("Enter choice: ");

            int choice = ConsoleInput.readInt();

            switch (choice) {
                case 1:
                    recordTransaction("INWARD");
                    break;
                case 2:
                    recordTransaction("OUTWARD");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void recordTransaction(String type) {
        System.out.print("Enter Product SKU: ");
        String sku = ConsoleInput.getScanner().nextLine();
        System.out.print("Enter Quantity: ");
        int quantity = ConsoleInput.readInt();

        presenter.processTransaction(sku, quantity, type, currentUser.getId());
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}

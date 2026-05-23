package com.gokulrajvel.gmart.features.billing;

import com.gokulrajvel.gmart.data.dto.Product;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.util.ConsoleInput;
import java.util.List;

public class BillingView {
    private BillingPresenter presenter;
    private User currentUser;

    public BillingView(User user) {
        this.currentUser = user;
        this.presenter = new BillingPresenter(this);
    }

    public void display() {
        while (true) {
            System.out.println("\n--- Billing System ---");
            System.out.println("1. Add Item by SKU");
            System.out.println("2. View Cart & Generate Receipt");
            System.out.println("3. Clear Cart");
            System.out.println("0. Back to Dashboard");
            System.out.print("Enter choice: ");

            int choice = ConsoleInput.readInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter Product SKU: ");
                    String sku = ConsoleInput.getScanner().nextLine();
                    presenter.processSku(sku);
                    break;
                case 2:
                    generateReceipt();
                    break;
                case 3:
                    presenter.clearCart();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public void promptForQuantity(Product product) {
        System.out.print("Enter Quantity for " + product.getName() + ": ");
        int quantity = ConsoleInput.readInt();
        presenter.addItemToCart(product, quantity);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    private void generateReceipt() {
        List<BillingModel.OrderItem> cart = presenter.getCart();
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        System.out.println("\n================ RECEIPT ================");
        System.out.printf("%-20s %-10s %-10s %-10s\n", "Item", "Price", "Qty", "Subtotal");
        for (BillingModel.OrderItem item : cart) {
            double subtotal = item.getProduct().getPrice() * item.getQuantity();
            System.out.printf("%-20s %-10.2f %-10d %-10.2f\n", 
                item.getProduct().getName(), item.getProduct().getPrice(), item.getQuantity(), subtotal);
        }
        System.out.println("-----------------------------------------");
        System.out.printf("Subtotal: %28.2f\n", presenter.getSubtotal());
        System.out.printf("Tax (5%%): %28.2f\n", presenter.getTax());
        System.out.println("-----------------------------------------");
        System.out.printf("TOTAL:    %28.2f\n", presenter.getTotal());
        System.out.println("=========================================");
        
        System.out.println("Select Payment Method:");
        System.out.println("1. CASH");
        System.out.println("2. CARD");
        System.out.println("3. UPI");
        System.out.print("Enter choice: ");
        int pChoice = ConsoleInput.readInt();
        String method = "CASH";
        if (pChoice == 2) method = "CARD";
        else if (pChoice == 3) method = "UPI";

        System.out.println("Confirm and Print Receipt? (y/n)");
        String confirm = ConsoleInput.getScanner().nextLine();
        if (confirm.equalsIgnoreCase("y")) {
            presenter.finalizeSale(currentUser.getId(), method);
        }
    }
}

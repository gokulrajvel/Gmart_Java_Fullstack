package com.gokulrajvel.gmart.features.dashboard;

import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.util.ConsoleInput;

public class DashboardView {
    private DashboardPresenter presenter;
    private User currentUser;
    private boolean isRunning = true;

    public DashboardView(User user) {
        this.currentUser = user;
        this.presenter = new DashboardPresenter(this);
    }

    public void displayMainMenu() {
        while (isRunning) {
            System.out.println("\n--- GMart Inventory Management System ---");
            if (currentUser.getRole() == Role.BILLING_STAFF) {
                System.out.println("1. Billing System");
                System.out.println("2. Logout");
                System.out.println("0. Exit");
            } else {
                System.out.println("1. Stock Management");
                System.out.println("2. Supplier Management");
                System.out.println("3. Inward and Outward Transactions");
                System.out.println("4. Inventory Tracking");
                System.out.println("5. Reports");
                System.out.println("6. Billing System");
                if (currentUser.getRole() == Role.ADMIN) {
                    System.out.println("7. User Management");
                }
                System.out.println("8. Logout");
                System.out.println("0. Exit");
            }
            System.out.print("Enter your choice: ");
            int choice = ConsoleInput.readInt();
            presenter.navigateTo(choice, currentUser);
        }
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void logout() {
        System.out.println("Logging out...");
        isRunning = false;
    }

    public void exit() {
        System.out.println("Exiting...");
        System.exit(0);
    }
}

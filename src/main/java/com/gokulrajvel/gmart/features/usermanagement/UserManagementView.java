package com.gokulrajvel.gmart.features.usermanagement;

import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.util.ConsoleInput;

public class UserManagementView {
    private UserManagementPresenter presenter;

    public UserManagementView() {
        this.presenter = new UserManagementPresenter(this);
    }

    public void display() {
        while (true) {
            System.out.println("\n--- User Management ---");
            System.out.println("1. Add Employee");
            System.out.println("2. View All Employees");
            System.out.println("0. Back to Dashboard");
            System.out.print("Enter your choice: ");

            int choice = ConsoleInput.readInt();

            switch (choice) {
                case 1:
                    addEmployee();
                    break;
                case 2:
                    viewEmployees();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void addEmployee() {
        System.out.println("\n--- Add New Employee ---");
        System.out.print("Username: ");
        String username = ConsoleInput.getScanner().nextLine();
        System.out.print("Password: ");
        String password = ConsoleInput.getScanner().nextLine();
        
        System.out.println("Select Role:");
        Role[] roles = Role.values();
        for (int i = 0; i < roles.length; i++) {
            System.out.println((i + 1) + ". " + roles[i]);
        }
        System.out.print("Enter choice (1-" + roles.length + "): ");
        int roleChoice = ConsoleInput.readInt();

        if (roleChoice > 0 && roleChoice <= roles.length) {
            Role role = roles[roleChoice - 1];
            presenter.addUser(username, password, role);
        } else {
            System.out.println("Invalid role choice.");
        }
    }

    private void viewEmployees() {
        System.out.println("\n--- Employee List ---");
        for (User user : presenter.getUsers()) {
            System.out.println(user);
        }
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}

package com.gokulrajvel.gmart.features.login;

import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.features.dashboard.DashboardView;
import com.gokulrajvel.gmart.util.ConsoleInput;

public class LoginView {
    private LoginPresenter presenter;

    public LoginView() {
        this.presenter = new LoginPresenter(this);
    }

    public void displayLogin() {
        while (true) {
            System.out.println("--- GMart Login ---");
            System.out.print("Username: ");
            String username = ConsoleInput.getScanner().nextLine();
            System.out.print("Password: ");
            String password = ConsoleInput.getScanner().nextLine();

            presenter.login(username, password);
            // The presenter will call onLoginSuccess which starts the dashboard,
            // or showMessage and then we loop again.
        }
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void onLoginSuccess(User user) {
        DashboardView dashboardView = new DashboardView(user);
        dashboardView.displayMainMenu();
    }
}

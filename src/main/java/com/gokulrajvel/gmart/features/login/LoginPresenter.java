package com.gokulrajvel.gmart.features.login;

import com.gokulrajvel.gmart.data.dto.User;

public class LoginPresenter {
    private LoginView view;
    private LoginModel model;

    public LoginPresenter(LoginView view) {
        this.view = view;
        this.model = new LoginModel(this);
    }

    public void login(String username, String password) {
        User user = model.authenticate(username, password);
        if (user != null) {
            view.showMessage("Login successful! Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
            view.onLoginSuccess(user);
        } else {
            view.showMessage("Invalid username or password. Please try again.");
        }
    }
}

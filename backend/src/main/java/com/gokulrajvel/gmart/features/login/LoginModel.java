package com.gokulrajvel.gmart.features.login;

import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.data.repository.GmartDB;

public class LoginModel {
    private LoginPresenter presenter;

    public LoginModel(LoginPresenter presenter) {
        this.presenter = presenter;
    }

    public User authenticate(String username, String password) {
        return GmartDB.getInstance().authenticate(username, password);
    }
}

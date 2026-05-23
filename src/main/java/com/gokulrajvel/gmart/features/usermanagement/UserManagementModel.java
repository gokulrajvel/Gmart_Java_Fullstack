package com.gokulrajvel.gmart.features.usermanagement;

import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.data.repository.GmartDB;
import java.util.List;

public class UserManagementModel {
    private UserManagementPresenter presenter;

    public UserManagementModel(UserManagementPresenter presenter) {
        this.presenter = presenter;
    }

    public void addUser(String username, String password, Role role) {
        User user = new User(username, password, role);
        GmartDB.getInstance().addUser(user);
    }

    public List<User> getAllUsers() {
        return GmartDB.getInstance().getUsers();
    }
}

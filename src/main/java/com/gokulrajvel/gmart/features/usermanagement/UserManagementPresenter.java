package com.gokulrajvel.gmart.features.usermanagement;

import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.data.dto.User;
import java.util.List;

public class UserManagementPresenter {
    private UserManagementView view;
    private UserManagementModel model;

    public UserManagementPresenter(UserManagementView view) {
        this.view = view;
        this.model = new UserManagementModel(this);
    }

    public void addUser(String username, String password, Role role) {
        model.addUser(username, password, role);
        view.showMessage("Employee added successfully!");
    }

    public List<User> getUsers() {
        return model.getAllUsers();
    }
}

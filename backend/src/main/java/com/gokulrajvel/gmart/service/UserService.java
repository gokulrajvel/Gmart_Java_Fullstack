package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        System.out.println(user.get().getPassword());
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user.get();
        }
        return null;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}

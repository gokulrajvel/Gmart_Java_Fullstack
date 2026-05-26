package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String storedPassword = user.getPassword();
            if (passwordEncoder.matches(password, storedPassword)) {
                // Auto-upgrade plain-text password to BCrypt in database
                boolean isBcrypt = storedPassword != null && (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$"));
                if (!isBcrypt) {
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                    System.out.println("Migrated plain-text password to BCrypt for user: " + username);
                }
                return user;
            }
        }
        return null;
    }

    public User saveUser(User user) {
        String rawPassword = user.getPassword();
        boolean isBcrypt = rawPassword != null && (rawPassword.startsWith("$2a$") || rawPassword.startsWith("$2b$") || rawPassword.startsWith("$2y$"));
        if (!isBcrypt) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        return userRepository.save(user);
    }

    public Optional<User> updateUser(int id, User updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setUsername(updatedUser.getUsername());
            String rawPassword = updatedUser.getPassword();
            if (rawPassword != null && !rawPassword.isEmpty()) {
                boolean isBcrypt = rawPassword.startsWith("$2a$") || rawPassword.startsWith("$2b$") || rawPassword.startsWith("$2y$");
                if (!isBcrypt) {
                    existingUser.setPassword(passwordEncoder.encode(rawPassword));
                } else {
                    existingUser.setPassword(rawPassword);
                }
            }
            existingUser.setRole(updatedUser.getRole());
            existingUser.setAddress(updatedUser.getAddress());
            existingUser.setPhone(updatedUser.getPhone());
            existingUser.setAadharNo(updatedUser.getAadharNo());
            return userRepository.save(existingUser);
        });
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}

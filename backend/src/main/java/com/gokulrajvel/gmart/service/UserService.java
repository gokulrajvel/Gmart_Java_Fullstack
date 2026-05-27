package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Service class that handles business operations for Users/Employees.
 * Manages database authentication, password hashing, and user profile management (CRUD).
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs the user service with required repositories and encoders.
     *
     * @param userRepository  user persistence repository
     * @param passwordEncoder encoder used to hash passwords and verify matches
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates a user based on their username and password.
     * Integrates automatic hashing upgrade for legacy plain-text passwords stored in the database.
     *
     * @param username the input username
     * @param password the input plain-text password
     * @return the authenticated User object, or null if credentials do not match
     */
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

    /**
     * Saves a new user to the database.
     * Checks if the password is already BCrypt hashed; if not, hashes it before persisting.
     *
     * @param user the User entity details to save
     * @return the saved User entity
     */
    public User saveUser(User user) {
        String rawPassword = user.getPassword();
        boolean isBcrypt = rawPassword != null && (rawPassword.startsWith("$2a$") || rawPassword.startsWith("$2b$") || rawPassword.startsWith("$2y$"));
        if (!isBcrypt) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        return userRepository.save(user);
    }

    /**
     * Updates an existing user's profile information.
     * Dynamically detects password modifications and hashes them if updated.
     *
     * @param id          the ID of the user to update
     * @param updatedUser the new profile details to merge
     * @return an Optional containing the updated User if found, or empty if the ID does not exist
     */
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

    /**
     * Retrieves all user accounts.
     *
     * @return a List of all User entities
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete
     */
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}

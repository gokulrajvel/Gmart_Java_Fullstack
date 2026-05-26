package com.gokulrajvel.gmart.config;

import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // If the database has no users, create the default Admin account
        java.util.Optional<User> adminOpt = userRepository.findByUsername("Gokulraj");
        if (adminOpt.isPresent()) {
            User admin = adminOpt.get();
            // If the password matches the default plain text password, migrate it to BCrypt
            if ("Gokulraj@2".equals(admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode("Gokulraj@2"));
                userRepository.save(admin);
                System.out.println("Existing plain-text admin password migrated to BCrypt successfully.");
            }
        } else if (userRepository.count() == 0) {
            User defaultAdmin = new User("Gokulraj", passwordEncoder.encode("Gokulraj@2"), Role.ADMIN);
            userRepository.save(defaultAdmin);
            System.out.println("New database environment detected. Default admin created: Gokulraj / Gokulraj@2");
        }
    }
}

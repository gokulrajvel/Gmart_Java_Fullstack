package com.gokulrajvel.gmart.config;

import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    public DatabaseSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // If the database has no users, create the default Admin account
        if (userRepository.count() == 0) {
            User defaultAdmin = new User("Gokulraj", "Gokulraj@2", Role.ADMIN);
            userRepository.save(defaultAdmin);
            System.out.println("New database environment detected. Default admin created: Gokulraj / Gokulraj@2");
        }
    }
}

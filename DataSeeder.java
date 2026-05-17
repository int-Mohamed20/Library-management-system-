package com.library.config;

import com.library.model.User;
import com.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@library.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@library.com");
            admin.setPassword("admin123");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
        }
    }
}

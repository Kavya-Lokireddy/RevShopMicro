package com.revshop.auth.config;

import com.revshop.auth.entity.Role;
import com.revshop.auth.entity.User;
import com.revshop.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data loader to seed sample users into the database on startup.
 * Creates test users for both BUYER and SELLER roles.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only seed if database is empty
        if (userRepository.count() == 0) {
            // Create sample buyer
            User buyer = new User();
            buyer.setName("John Buyer");
            buyer.setEmail("buyer@revshop.com");
            buyer.setPassword(passwordEncoder.encode("password123"));
            buyer.setRole(Role.BUYER);
            buyer.setActive(true);
            userRepository.save(buyer);

            // Create sample seller
            User seller = new User();
            seller.setName("Jane Seller");
            seller.setEmail("seller@revshop.com");
            seller.setPassword(passwordEncoder.encode("password123"));
            seller.setRole(Role.SELLER);
            seller.setBusinessName("Jane's Electronics");
            seller.setActive(true);
            userRepository.save(seller);

            // Create additional sample users
            User buyer2 = new User();
            buyer2.setName("Alice Customer");
            buyer2.setEmail("alice@example.com");
            buyer2.setPassword(passwordEncoder.encode("password123"));
            buyer2.setRole(Role.BUYER);
            buyer2.setActive(true);
            userRepository.save(buyer2);

            User seller2 = new User();
            seller2.setName("Bob Merchant");
            seller2.setEmail("bob@example.com");
            seller2.setPassword(passwordEncoder.encode("password123"));
            seller2.setRole(Role.SELLER);
            seller2.setBusinessName("Bob's Store");
            seller2.setActive(true);
            userRepository.save(seller2);

            System.out.println("Sample users created successfully!");
            System.out.println("Buyer: buyer@revshop.com / password123");
            System.out.println("Seller: seller@revshop.com / password123");
        }
    }
}

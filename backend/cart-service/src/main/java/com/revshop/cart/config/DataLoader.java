package com.revshop.cart.config;

import com.revshop.cart.model.Cart;
import com.revshop.cart.model.CartItem;
import com.revshop.cart.model.Favorite;
import com.revshop.cart.repository.CartRepository;
import com.revshop.cart.repository.FavoriteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(CartRepository cartRepository,
                                     FavoriteRepository favoriteRepository) {
        return args -> {
            // Sample data for testing
            if (cartRepository.count() == 0) {
                // Create sample cart for seeded buyer user 1
                Cart cart1 = new Cart(1L);
                CartItem item1 = new CartItem(1L, "Wireless Headphones", 79.99, 2L, 1);
                CartItem item2 = new CartItem(2L, "Smart Watch", 199.99, 2L, 1);
                cart1.addItem(item1);
                cart1.addItem(item2);
                cartRepository.save(cart1);

                // Create sample cart for seeded buyer user 3
                Cart cart2 = new Cart(3L);
                CartItem item3 = new CartItem(3L, "Men's T-Shirt", 19.99, 4L, 2);
                cart2.addItem(item3);
                cartRepository.save(cart2);

                System.out.println("Sample carts loaded successfully!");
            }

            if (favoriteRepository.count() == 0) {
                // Create sample favorites
                Favorite fav1 = new Favorite(1L, 1L, "Wireless Headphones", 79.99,
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e");
                Favorite fav2 = new Favorite(1L, 2L, "Smart Watch", 199.99,
                        "https://images.unsplash.com/photo-1523275335684-37898b6baf30");
                Favorite fav3 = new Favorite(3L, 3L, "Men's T-Shirt", 19.99,
                        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab");

                favoriteRepository.save(fav1);
                favoriteRepository.save(fav2);
                favoriteRepository.save(fav3);

                System.out.println("Sample favorites loaded successfully!");
            }
        };
    }
}

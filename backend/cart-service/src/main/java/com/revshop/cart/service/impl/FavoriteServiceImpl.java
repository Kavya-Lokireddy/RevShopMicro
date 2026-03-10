package com.revshop.cart.service.impl;

import com.revshop.cart.client.ProductServiceClient;
import com.revshop.cart.dto.FavoriteResponse;
import com.revshop.cart.dto.ProductDto;
import com.revshop.cart.exception.DuplicateResourceException;
import com.revshop.cart.exception.ResourceNotFoundException;
import com.revshop.cart.model.Favorite;
import com.revshop.cart.repository.FavoriteRepository;
import com.revshop.cart.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductServiceClient productServiceClient;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository,
                              ProductServiceClient productServiceClient) {
        this.favoriteRepository = favoriteRepository;
        this.productServiceClient = productServiceClient;
    }

    @Override
    @Transactional
    public FavoriteResponse addFavorite(Long userId, Long productId) {
        // Check if already favorited
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new DuplicateResourceException("Product is already in your favorites");
        }

        // Get product details from product-service
        ProductDto product = productServiceClient.getProduct(productId);

        if (product == null || product.getId() == null) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        // Create and save favorite
        Favorite favorite = new Favorite(
                userId,
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl()
        );

        Favorite savedFavorite = favoriteRepository.save(favorite);
        return mapToResponse(savedFavorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));

        favoriteRepository.delete(favorite);
    }

    @Override
    public List<FavoriteResponse> getFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    // Helper Method
    private FavoriteResponse mapToResponse(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                favorite.getProductId(),
                favorite.getProductName(),
                favorite.getProductPrice(),
                favorite.getProductImage(),
                favorite.getAddedAt()
        );
    }
}

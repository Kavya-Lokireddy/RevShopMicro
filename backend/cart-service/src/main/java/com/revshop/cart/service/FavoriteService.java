package com.revshop.cart.service;

import com.revshop.cart.dto.FavoriteResponse;

import java.util.List;

public interface FavoriteService {

    FavoriteResponse addFavorite(Long userId, Long productId);

    void removeFavorite(Long userId, Long productId);

    List<FavoriteResponse> getFavorites(Long userId);

    boolean isFavorite(Long userId, Long productId);
}

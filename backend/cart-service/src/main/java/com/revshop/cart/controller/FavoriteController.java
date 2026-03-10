package com.revshop.cart.controller;

import com.revshop.cart.dto.FavoriteResponse;
import com.revshop.cart.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<FavoriteResponse> addFavorite(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {
        FavoriteResponse response = favoriteService.addFavorite(userId, productId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFavorite(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {
        favoriteService.removeFavorite(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> getFavorites(
            @RequestHeader("X-User-Id") Long userId) {
        List<FavoriteResponse> favorites = favoriteService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<Map<String, Boolean>> isFavorite(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {
        boolean isFav = favoriteService.isFavorite(userId, productId);
        return ResponseEntity.ok(Map.of("isFavorite", isFav));
    }
}

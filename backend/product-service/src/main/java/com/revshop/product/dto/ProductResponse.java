package com.revshop.product.dto;

import java.time.LocalDateTime;

public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double mrp;
    private Double discountPercentage;
    private Integer quantity;
    private Double rating;
    private String imageUrl;
    private Boolean active;
    private Integer stockThreshold;
    private Long sellerId;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, String description, Double price, Double mrp,
                           Double discountPercentage, Integer quantity, Double rating, String imageUrl,
                           Boolean active, Integer stockThreshold, Long sellerId, Long categoryId,
                           String categoryName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.mrp = mrp;
        this.discountPercentage = discountPercentage;
        this.quantity = quantity;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.active = active;
        this.stockThreshold = stockThreshold;
        this.sellerId = sellerId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private Double mrp;
        private Double discountPercentage;
        private Integer quantity;
        private Double rating;
        private String imageUrl;
        private Boolean active;
        private Integer stockThreshold;
        private Long sellerId;
        private Long categoryId;
        private String categoryName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(Double price) { this.price = price; return this; }
        public Builder mrp(Double mrp) { this.mrp = mrp; return this; }
        public Builder discountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder rating(Double rating) { this.rating = rating; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }
        public Builder stockThreshold(Integer stockThreshold) { this.stockThreshold = stockThreshold; return this; }
        public Builder sellerId(Long sellerId) { this.sellerId = sellerId; return this; }
        public Builder categoryId(Long categoryId) { this.categoryId = categoryId; return this; }
        public Builder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ProductResponse build() {
            return new ProductResponse(id, name, description, price, mrp, discountPercentage, quantity,
                    rating, imageUrl, active, stockThreshold, sellerId, categoryId, categoryName, createdAt, updatedAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getMrp() { return mrp; }
    public void setMrp(Double mrp) { this.mrp = mrp; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getStockThreshold() { return stockThreshold; }
    public void setStockThreshold(Integer stockThreshold) { this.stockThreshold = stockThreshold; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

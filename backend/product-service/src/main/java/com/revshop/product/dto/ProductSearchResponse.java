package com.revshop.product.dto;

import java.util.List;

public class ProductSearchResponse {

    private List<ProductResponse> products;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;

    public ProductSearchResponse() {
    }

    public ProductSearchResponse(List<ProductResponse> products, int currentPage, int totalPages, long totalItems, int pageSize) {
        this.products = products;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<ProductResponse> products;
        private int currentPage;
        private int totalPages;
        private long totalItems;
        private int pageSize;

        public Builder products(List<ProductResponse> products) { this.products = products; return this; }
        public Builder currentPage(int currentPage) { this.currentPage = currentPage; return this; }
        public Builder totalPages(int totalPages) { this.totalPages = totalPages; return this; }
        public Builder totalItems(long totalItems) { this.totalItems = totalItems; return this; }
        public Builder pageSize(int pageSize) { this.pageSize = pageSize; return this; }

        public ProductSearchResponse build() {
            return new ProductSearchResponse(products, currentPage, totalPages, totalItems, pageSize);
        }
    }

    public List<ProductResponse> getProducts() { return products; }
    public void setProducts(List<ProductResponse> products) { this.products = products; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}

package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.dto.Product;
import com.gokulrajvel.gmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySkuCode(sku);
    }

    public Optional<Product> getProductById(int id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void updateStock(int productId, int quantityChange) {
        productRepository.findById(productId).ifPresent(product -> {
            product.setStockQuantity(product.getStockQuantity() + quantityChange);
            productRepository.save(product);
        });
    }
}

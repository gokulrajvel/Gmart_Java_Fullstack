package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.dto.Product;
import com.gokulrajvel.gmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final StockAlertNotificationService alertService;

    public ProductService(ProductRepository productRepository, StockAlertNotificationService alertService) {
        this.productRepository = productRepository;
        this.alertService = alertService;
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
        Product saved = productRepository.save(product);
        alertService.checkAndSendStockAlert(saved.getSkuCode(), saved.getName(), saved.getStockQuantity(), 10);
        return saved;
    }

    public Optional<Product> updateProduct(int id, Product updatedProduct) {
        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setSkuCode(updatedProduct.getSkuCode());
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setCategoryId(updatedProduct.getCategoryId());
            existingProduct.setSupplierId(updatedProduct.getSupplierId());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
            existingProduct.setDiscount(updatedProduct.getDiscount());
            existingProduct.setGst(updatedProduct.getGst());
            Product saved = productRepository.save(existingProduct);
            alertService.checkAndSendStockAlert(saved.getSkuCode(), saved.getName(), saved.getStockQuantity(), 10);
            return saved;
        });
    }

    public void updateStock(int productId, int quantityChange) {
        productRepository.findById(productId).ifPresent(product -> {
            product.setStockQuantity(product.getStockQuantity() + quantityChange);
            Product saved = productRepository.save(product);
            alertService.checkAndSendStockAlert(saved.getSkuCode(), saved.getName(), saved.getStockQuantity(), 10);
        });
    }
}

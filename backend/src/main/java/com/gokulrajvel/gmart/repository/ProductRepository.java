package com.gokulrajvel.gmart.repository;

import com.gokulrajvel.gmart.data.dto.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findBySkuCode(String skuCode);
}

package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.dto.InventoryTransaction;
import com.gokulrajvel.gmart.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ProductService productService;

    public TransactionService(TransactionRepository transactionRepository, ProductService productService) {
        this.transactionRepository = transactionRepository;
        this.productService = productService;
    }

    public List<InventoryTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public InventoryTransaction recordTransaction(InventoryTransaction transaction) {
        if (transaction.getQuantity() <= 0) {
            throw new RuntimeException("Transaction quantity must be positive");
        }

        var productOpt = productService.getProductById(transaction.getProductId());
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found");
        }

        if ("OUTWARD".equals(transaction.getTransactionType())) {
            if (productOpt.get().getStockQuantity() < transaction.getQuantity()) {
                throw new RuntimeException("Insufficient stock for OUTWARD transaction");
            }
        } else if (!"INWARD".equals(transaction.getTransactionType())) {
            throw new RuntimeException("Invalid transaction type");
        }

        int adjustment = "INWARD".equals(transaction.getTransactionType()) ? transaction.getQuantity() : -transaction.getQuantity();
        productService.updateStock(transaction.getProductId(), adjustment);
        return transactionRepository.save(transaction);
    }
}

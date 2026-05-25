package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.dto.InventoryTransaction;
import com.gokulrajvel.gmart.data.dto.Product;
import com.gokulrajvel.gmart.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    private TransactionService transactionService;
    private TransactionRepository mockTransactionRepository;
    private ProductService mockProductService;

    @BeforeEach
    public void setUp() {
        mockTransactionRepository = mock(TransactionRepository.class);
        mockProductService = mock(ProductService.class);
        transactionService = new TransactionService(mockTransactionRepository, mockProductService);
    }

    @Test
    public void testRecordTransaction_OutwardInsufficientStock() {
        InventoryTransaction transaction = new InventoryTransaction(1, 1, "OUTWARD", 10);
        Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 5); // Only 5 in stock
        
        when(mockProductService.getProductById(1)).thenReturn(Optional.of(product));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.recordTransaction(transaction);
        });
        
        assertEquals("Insufficient stock for OUTWARD transaction", exception.getMessage());
        verify(mockProductService, never()).updateStock(anyInt(), anyInt());
        verify(mockTransactionRepository, never()).save(any());
    }

    @Test
    public void testRecordTransaction_OutwardSuccess() {
        InventoryTransaction transaction = new InventoryTransaction(1, 1, "OUTWARD", 5);
        Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 10); // 10 in stock
        
        when(mockProductService.getProductById(1)).thenReturn(Optional.of(product));
        when(mockTransactionRepository.save(transaction)).thenReturn(transaction);
        
        InventoryTransaction saved = transactionService.recordTransaction(transaction);
        
        assertNotNull(saved);
        verify(mockProductService, times(1)).updateStock(1, -5);
        verify(mockTransactionRepository, times(1)).save(transaction);
    }

    @Test
    public void testRecordTransaction_NegativeQuantity() {
        InventoryTransaction transaction = new InventoryTransaction(1, 1, "OUTWARD", -5);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.recordTransaction(transaction);
        });
        
        assertEquals("Transaction quantity must be positive", exception.getMessage());
        verify(mockProductService, never()).getProductById(anyInt());
        verify(mockTransactionRepository, never()).save(any());
    }

    @Test
    public void testRecordTransaction_ZeroQuantity() {
        InventoryTransaction transaction = new InventoryTransaction(1, 1, "INWARD", 0);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.recordTransaction(transaction);
        });
        
        assertEquals("Transaction quantity must be positive", exception.getMessage());
        verify(mockProductService, never()).getProductById(anyInt());
        verify(mockTransactionRepository, never()).save(any());
    }

    @Test
    public void testRecordTransaction_ProductNotFound() {
        InventoryTransaction transaction = new InventoryTransaction(1, 1, "INWARD", 5);
        
        when(mockProductService.getProductById(1)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.recordTransaction(transaction);
        });
        
        assertEquals("Product not found", exception.getMessage());
        verify(mockProductService, never()).updateStock(anyInt(), anyInt());
        verify(mockTransactionRepository, never()).save(any());
    }

    @Test
    public void testRecordTransaction_InvalidType() {
        InventoryTransaction transaction = new InventoryTransaction(1, 1, "INVALID", 5);
        Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 10);
        
        when(mockProductService.getProductById(1)).thenReturn(Optional.of(product));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.recordTransaction(transaction);
        });
        
        assertEquals("Invalid transaction type", exception.getMessage());
        verify(mockProductService, never()).updateStock(anyInt(), anyInt());
        verify(mockTransactionRepository, never()).save(any());
    }

    @Test
    public void testRecordTransaction_InwardSuccess() {
        InventoryTransaction transaction = new InventoryTransaction(1, 1, "INWARD", 5);
        Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 10);
        
        when(mockProductService.getProductById(1)).thenReturn(Optional.of(product));
        when(mockTransactionRepository.save(transaction)).thenReturn(transaction);
        
        InventoryTransaction saved = transactionService.recordTransaction(transaction);
        
        assertNotNull(saved);
        verify(mockProductService, times(1)).updateStock(1, 5);
        verify(mockTransactionRepository, times(1)).save(transaction);
    }
}

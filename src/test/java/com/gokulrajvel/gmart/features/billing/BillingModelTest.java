package com.gokulrajvel.gmart.features.billing;

import com.gokulrajvel.gmart.data.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BillingModelTest {

    private BillingModel billingModel;
    private BillingPresenter mockPresenter;

    @BeforeEach
    public void setUp() {
        mockPresenter = mock(BillingPresenter.class);
        billingModel = new BillingModel(mockPresenter);
    }

    @Test
    public void testAddToCart_Success() {
        Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 10);
        boolean result = billingModel.addToCart(product, 5);
        
        assertTrue(result);
        assertEquals(1, billingModel.getCart().size());
        assertEquals(5, billingModel.getCart().get(0).getQuantity());
    }

    @Test
    public void testAddToCart_InsufficientStock() {
        Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 5);
        boolean result = billingModel.addToCart(product, 10);
        
        assertFalse(result);
        assertEquals(0, billingModel.getCart().size());
    }

    @Test
    public void testAddToCart_AccumulatedQuantityExceedsStock() {
        Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 10);
        
        boolean firstAdd = billingModel.addToCart(product, 6);
        assertTrue(firstAdd);
        
        boolean secondAdd = billingModel.addToCart(product, 5); // 6 + 5 = 11 > 10
        assertFalse(secondAdd);
        
        assertEquals(1, billingModel.getCart().size());
        assertEquals(6, billingModel.getCart().get(0).getQuantity());
    }

    @Test
    public void testAddToCart_NegativeQuantity() {
        Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 10);
        boolean result = billingModel.addToCart(product, -5);
        
        assertFalse(result);
        assertEquals(0, billingModel.getCart().size());
    }

    @Test
    public void testAddToCart_ZeroQuantity() {
        Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 10);
        boolean result = billingModel.addToCart(product, 0);
        
        assertFalse(result);
        assertEquals(0, billingModel.getCart().size());
    }

    @Test
    public void testClearCart() {
        Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 10);
        billingModel.addToCart(product, 3);
        assertEquals(1, billingModel.getCart().size());
        
        billingModel.clearCart();
        assertEquals(0, billingModel.getCart().size());
    }

    @Test
    public void testCalculations_EmptyCart() {
        assertEquals(0.0, billingModel.calculateSubtotal(), 0.001);
        assertEquals(0.0, billingModel.calculateTax(), 0.001);
        assertEquals(0.0, billingModel.calculateTotal(), 0.001);
    }

    @Test
    public void testCalculations_MultipleItems() {
        Product p1 = new Product(1, "SKU1", "Product 1", 1, 1, 10.0, 10);
        Product p2 = new Product(2, "SKU2", "Product 2", 1, 1, 20.0, 10);
        
        billingModel.addToCart(p1, 2); // 20.0
        billingModel.addToCart(p2, 3); // 60.0
        
        double expectedSubtotal = 80.0;
        double expectedTax = 4.0; // 5% of 80
        double expectedTotal = 84.0;
        
        assertEquals(expectedSubtotal, billingModel.calculateSubtotal(), 0.001);
        assertEquals(expectedTax, billingModel.calculateTax(), 0.001);
        assertEquals(expectedTotal, billingModel.calculateTotal(), 0.001);
    }

    @Test
    public void testFinalizeSale_Success() throws Exception {
        // Mock GmartDB
        com.gokulrajvel.gmart.data.repository.GmartDB mockDb = mock(com.gokulrajvel.gmart.data.repository.GmartDB.class);
        
        // Inject mockDb into static instance field using reflection
        java.lang.reflect.Field instanceField = com.gokulrajvel.gmart.data.repository.GmartDB.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        com.gokulrajvel.gmart.data.repository.GmartDB originalInstance = (com.gokulrajvel.gmart.data.repository.GmartDB) instanceField.get(null);
        instanceField.set(null, mockDb);
        
        try {
            Product product = new Product(1, "SKU123", "Test Product", 1, 1, 10.0, 10);
            billingModel.addToCart(product, 2);
            
            billingModel.finalizeSale(1, "CASH");
            
            // Verify DB calls
            verify(mockDb, times(1)).updateProductStock(1, -2);
            verify(mockDb, times(1)).recordTransaction(any(InventoryTransaction.class));
            verify(mockDb, times(1)).createBill(any(Bill.class), anyList());
            
            // Cart should be empty after finalize
            assertEquals(0, billingModel.getCart().size());
        } finally {
            // Restore original instance
            instanceField.set(null, originalInstance);
        }
    }
}

package com.gokulrajvel.gmart.data.dto;

import com.gokulrajvel.gmart.data.PaymentMethod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BillTest {

    @Test
    public void testPaymentMethodTrimAndUpperCase() {
        Bill bill = new Bill(1, 100.0, 5.0, "  cash  ");
        assertEquals("CASH", bill.getPaymentMethod());
        
        bill.setPaymentMethod("  card  ");
        assertEquals("CARD", bill.getPaymentMethod());
        
        bill.setPaymentMethod("Upi");
        assertEquals("UPI", bill.getPaymentMethod());
    }

    @Test
    public void testPaymentMethodNull() {
        Bill bill = new Bill(1, 100.0, 5.0, null);
        assertNull(bill.getPaymentMethod());
        
        bill.setPaymentMethod(null);
        assertNull(bill.getPaymentMethod());
    }

    @Test
    public void testPaymentMethodInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Bill(1, 100.0, 5.0, "BITCOIN");
        });
        
        Bill bill = new Bill(1, 100.0, 5.0, "CASH");
        assertThrows(IllegalArgumentException.class, () -> {
            bill.setPaymentMethod("INVALID_METHOD");
        });
    }

    @Test
    public void testBillGettersSettersAndDefaults() {
        Bill bill = new Bill();
        bill.setId(99);
        bill.setUserId(42);
        bill.setTotalAmount(250.0);
        bill.setTaxAmount(12.5);
        
        assertEquals(99, bill.getId());
        assertEquals(42, bill.getUserId());
        assertEquals(250.0, bill.getTotalAmount());
        assertEquals(12.5, bill.getTaxAmount());
        assertNotNull(bill.getBillDate());
    }
}

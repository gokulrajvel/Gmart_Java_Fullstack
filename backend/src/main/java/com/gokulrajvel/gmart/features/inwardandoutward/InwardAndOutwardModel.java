package com.gokulrajvel.gmart.features.inwardandoutward;

import com.gokulrajvel.gmart.data.dto.InventoryTransaction;
import com.gokulrajvel.gmart.data.dto.Product;
import com.gokulrajvel.gmart.data.repository.GmartDB;

public class InwardAndOutwardModel {
    private InwardandOutwardPresenter presenter;

    public InwardAndOutwardModel(InwardandOutwardPresenter presenter) {
        this.presenter = presenter;
    }

    public Product getProductBySku(String sku) {
        return GmartDB.getInstance().getProductBySku(sku);
    }

    public void recordTransaction(Product product, int quantity, String type, int userId) {
        int quantityChange = type.equals("INWARD") ? quantity : -quantity;
        
        // Update stock
        GmartDB.getInstance().updateProductStock(product.getId(), quantityChange);
        
        // Record transaction DTO
        InventoryTransaction transaction = new InventoryTransaction(
            product.getId(),
            userId,
            type,
            quantity
        );
        GmartDB.getInstance().recordTransaction(transaction);
    }
}

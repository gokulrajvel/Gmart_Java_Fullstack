package com.gokulrajvel.gmart.features.inventorytracking;

import com.gokulrajvel.gmart.data.dto.Product;
import com.gokulrajvel.gmart.data.repository.GmartDB;
import java.util.List;

public class InventoryModel {
    private InventoryPresenter presenter;

    public InventoryModel(InventoryPresenter presenter) {
        this.presenter = presenter;
    }

    public List<Product> getAllProducts() {
        return GmartDB.getInstance().getAllProducts();
    }
}

package com.gokulrajvel.gmart.features.inventorytracking;

import com.gokulrajvel.gmart.data.dto.Product;
import java.util.List;

public class InventoryPresenter {
    private InventoryView view;
    private InventoryModel model;

    public InventoryPresenter(InventoryView view) {
        this.view = view;
        this.model = new InventoryModel(this);
    }

    public List<Product> getProducts() {
        return model.getAllProducts();
    }
}

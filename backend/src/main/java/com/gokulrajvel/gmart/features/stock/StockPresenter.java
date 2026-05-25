package com.gokulrajvel.gmart.features.stock;

import com.gokulrajvel.gmart.data.dto.Supplier;
import java.util.List;

public class StockPresenter {
    private StockView view;
    private StockModel model;

    public StockPresenter(StockView view) {
        this.view = view;
        this.model = new StockModel(this);
    }

    public void addCategory(String name) {
        model.addCategory(name);
        view.showMessage("Category added successfully!");
    }

    public List<String> getCategories() {
        return model.getCategories();
    }

    public List<Supplier> getSuppliers() {
        return model.getSuppliers();
    }

    public void addProduct(String sku, String name, int catId, int suppId, double price, int stock) {
        model.addProduct(sku, name, catId, suppId, price, stock);
        view.showMessage("Product added successfully!");
    }
}

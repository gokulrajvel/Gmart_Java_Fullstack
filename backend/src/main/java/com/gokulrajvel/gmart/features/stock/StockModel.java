package com.gokulrajvel.gmart.features.stock;

import com.gokulrajvel.gmart.data.dto.Product;
import com.gokulrajvel.gmart.data.repository.GmartDB;
import java.util.List;

public class StockModel {
    private StockPresenter presenter;

    public StockModel(StockPresenter presenter) {
        this.presenter = presenter;
    }

    public List<String> getCategories() {
        return GmartDB.getInstance().getCategories();
    }

    public void addCategory(String name) {
        GmartDB.getInstance().addCategory(name);
    }

    public List<com.gokulrajvel.gmart.data.dto.Supplier> getSuppliers() {
        return GmartDB.getInstance().getAllSuppliers();
    }

    public void addProduct(String sku, String name, int categoryId, int supplierId, double price, int stock) {
        Product product = new Product(sku, name, categoryId, supplierId, price, stock);
        GmartDB.getInstance().addProduct(product);
    }
}

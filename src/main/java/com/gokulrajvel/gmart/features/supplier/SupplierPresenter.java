package com.gokulrajvel.gmart.features.supplier;

import com.gokulrajvel.gmart.data.dto.Supplier;
import java.util.List;

public class SupplierPresenter {
    private SupplierView view;
    private SupplierModel model;

    public SupplierPresenter(SupplierView view) {
        this.view = view;
        this.model = new SupplierModel(this);
    }

    public void addSupplier(String name, String contact) {
        model.addSupplier(name, contact);
        view.showMessage("Supplier added successfully!");
    }

    public List<Supplier> getSuppliers() {
        return model.getAllSuppliers();
    }
}

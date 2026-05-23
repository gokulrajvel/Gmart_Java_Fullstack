package com.gokulrajvel.gmart.features.supplier;

import com.gokulrajvel.gmart.data.dto.Supplier;
import com.gokulrajvel.gmart.data.repository.GmartDB;
import java.util.List;

public class SupplierModel {
    private SupplierPresenter presenter;

    public SupplierModel(SupplierPresenter presenter) {
        this.presenter = presenter;
    }

    public List<Supplier> getAllSuppliers() {
        return GmartDB.getInstance().getAllSuppliers();
    }

    public void addSupplier(String name, String contact) {
        Supplier supplier = new Supplier(name, contact);
        GmartDB.getInstance().addSupplier(supplier);
    }
}

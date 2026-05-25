package com.gokulrajvel.gmart.features.reporting;

import com.gokulrajvel.gmart.data.dto.InventoryTransaction;
import java.util.List;

public class ReportPresenter {
    private ReportView view;
    private ReportModel model;

    public ReportPresenter(ReportView view) {
        this.view = view;
        this.model = new ReportModel(this);
    }

    public List<InventoryTransaction> getTransactions() {
        return model.getTransactions();
    }
}

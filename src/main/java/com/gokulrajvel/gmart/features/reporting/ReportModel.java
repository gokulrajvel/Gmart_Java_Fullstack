package com.gokulrajvel.gmart.features.reporting;

import com.gokulrajvel.gmart.data.dto.InventoryTransaction;
import com.gokulrajvel.gmart.data.repository.GmartDB;
import java.util.List;

public class ReportModel {
    private ReportPresenter presenter;

    public ReportModel(ReportPresenter presenter) {
        this.presenter = presenter;
    }

    public List<InventoryTransaction> getTransactions() {
        return GmartDB.getInstance().getAllTransactions();
    }
}

package com.gokulrajvel.gmart.features.dashboard;

import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.features.billing.BillingView;
import com.gokulrajvel.gmart.features.inventorytracking.InventoryView;
import com.gokulrajvel.gmart.features.inwardandoutward.InwardandOutwardView;
import com.gokulrajvel.gmart.features.reporting.ReportView;
import com.gokulrajvel.gmart.features.stock.StockView;
import com.gokulrajvel.gmart.features.supplier.SupplierView;
import com.gokulrajvel.gmart.features.usermanagement.UserManagementView;

public class DashboardPresenter {
    private DashboardView view;
    private DashboardModel model;

    public DashboardPresenter(DashboardView view) {
        this.view = view;
        this.model = new DashboardModel(this);
    }

    public void navigateTo(int choice, User currentUser) {
        if (currentUser.getRole() == Role.BILLING_STAFF) {
            handleBillingStaffChoice(choice, currentUser);
        } else {
            handleGeneralChoice(choice, currentUser);
        }
    }

    private void handleBillingStaffChoice(int choice, User currentUser) {
        switch (choice) {
            case 1: new BillingView(currentUser).display(); break;
            case 2: view.logout(); break;
            case 0: view.exit(); break;
            default: view.showMessage("Invalid choice.");
        }
    }

    private void handleGeneralChoice(int choice, User currentUser) {
        switch (choice) {
            case 1: new StockView().display(); break;
            case 2: new SupplierView().display(); break;
            case 3: new InwardandOutwardView(currentUser).display(); break;
            case 4: new InventoryView().display(); break;
            case 5: new ReportView().display(); break;
            case 6: new BillingView(currentUser).display(); break;
            case 7:
                if (currentUser.getRole() == Role.ADMIN) {
                    new UserManagementView().display();
                } else {
                    view.showMessage("Invalid choice.");
                }
                break;
            case 8: view.logout(); break;
            case 0: view.exit(); break;
            default: view.showMessage("Invalid choice.");
        }
    }
}

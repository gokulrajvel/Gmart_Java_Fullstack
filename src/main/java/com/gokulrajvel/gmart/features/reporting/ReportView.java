package com.gokulrajvel.gmart.features.reporting;

import com.gokulrajvel.gmart.data.dto.InventoryTransaction;
import com.gokulrajvel.gmart.util.ConsoleInput;
import java.util.List;

public class ReportView {
    private ReportPresenter presenter;

    public ReportView() {
        this.presenter = new ReportPresenter(this);
    }

    public void display() {
        System.out.println("\n--- Transaction Reports ---");
        List<InventoryTransaction> transactions = presenter.getTransactions();
        
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            System.out.printf("%-5s %-10s %-10s %-15s %-10s %-20s\n", 
                "ID", "ProdID", "UserID", "Type", "Qty", "Date");
            System.out.println("-------------------------------------------------------------------------");
            for (InventoryTransaction t : transactions) {
                System.out.printf("%-5d %-10d %-10d %-15s %-10d %-20s\n", 
                    t.getId(), t.getProductId(), t.getUserId(), t.getTransactionType(), 
                    t.getQuantity(), t.getTransactionDate());
            }
        }
        
        System.out.println("\nPress Enter to return to Dashboard...");
        ConsoleInput.getScanner().nextLine();
    }
}

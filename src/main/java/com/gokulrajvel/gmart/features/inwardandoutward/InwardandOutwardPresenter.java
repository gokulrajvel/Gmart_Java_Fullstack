package com.gokulrajvel.gmart.features.inwardandoutward;

import com.gokulrajvel.gmart.data.dto.Product;

public class InwardandOutwardPresenter {
    private InwardandOutwardView view;
    private InwardAndOutwardModel model;

    public InwardandOutwardPresenter(InwardandOutwardView view) {
        this.view = view;
        this.model = new InwardAndOutwardModel(this);
    }

    public void processTransaction(String sku, int quantity, String type, int userId) {
        if (quantity <= 0) {
            view.showMessage("Quantity must be greater than zero.");
            return;
        }
        Product product = model.getProductBySku(sku);
        if (product != null) {
            if (type.equals("OUTWARD") && product.getStockQuantity() < quantity) {
                view.showMessage("Insufficient stock! Available: " + product.getStockQuantity());
                return;
            }
            model.recordTransaction(product, quantity, type, userId);
            view.showMessage("Transaction recorded successfully!");
        } else {
            view.showMessage("Product not found.");
        }
    }
}

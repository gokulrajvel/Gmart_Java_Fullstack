package com.gokulrajvel.gmart.features.billing;

import com.gokulrajvel.gmart.data.dto.Product;
import java.util.List;

public class BillingPresenter {
    private BillingView view;
    private BillingModel model;

    public BillingPresenter(BillingView view) {
        this.view = view;
        this.model = new BillingModel(this);
    }

    public void processSku(String sku) {
        Product product = model.getProductBySku(sku);
        if (product != null) {
            view.promptForQuantity(product);
        } else {
            view.showMessage("Product not found.");
        }
    }

    public void addItemToCart(Product product, int quantity) {
        if (quantity <= 0) {
            view.showMessage("Quantity must be greater than zero.");
            return;
        }
        if (model.addToCart(product, quantity)) {
            view.showMessage("Added " + product.getName() + " to cart.");
        } else {
            view.showMessage("Insufficient stock! Available: " + product.getStockQuantity());
        }
    }

    public List<BillingModel.OrderItem> getCart() {
        return model.getCart();
    }

    public double getSubtotal() { return model.calculateSubtotal(); }
    public double getTax() { return model.calculateTax(); }
    public double getTotal() { return model.calculateTotal(); }

    public void clearCart() {
        model.clearCart();
        view.showMessage("Cart cleared.");
    }

    public void finalizeSale(int userId, String paymentMethod) {
        model.finalizeSale(userId, paymentMethod);
        view.showMessage("Receipt Printed, Stock Updated, and Bill Recorded successfully!");
    }
}

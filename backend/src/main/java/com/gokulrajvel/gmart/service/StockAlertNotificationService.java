package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockAlertNotificationService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public StockAlertNotificationService(UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Checks stock quantity and pushes alerts to active ADMIN and PURCHASING_MANAGER users.
     */
    public void checkAndSendStockAlert(String skuCode, String productName, int currentStock, int minThreshold) {
        if (currentStock <= minThreshold) {
            // Find all users who are ADMIN or PURCHASING_MANAGER
            List<User> targetUsers = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.PURCHASING_MANAGER)
                    .toList();

            StockAlertPayload payload = new StockAlertPayload(skuCode, productName, currentStock);

            for (User user : targetUsers) {
                // Spring handles routing this unicast message dynamically to the target user's session queue
                messagingTemplate.convertAndSendToUser(
                    user.getUsername(),
                    "/queue/notifications",
                    payload
                );
            }
        }
    }

    public static class StockAlertPayload {
        private final String skuCode;
        private final String productName;
        private final int currentStock;
        private final String alertMessage;

        public StockAlertPayload(String skuCode, String productName, int currentStock) {
            this.skuCode = skuCode;
            this.productName = productName;
            this.currentStock = currentStock;
            this.alertMessage = String.format("Stock Alert: Product '%s' (%s) is running low! Only %d left.", 
                    productName, skuCode, currentStock);
        }

        public String getSkuCode() { return skuCode; }
        public String getProductName() { return productName; }
        public int getCurrentStock() { return currentStock; }
        public String getAlertMessage() { return alertMessage; }
    }
}

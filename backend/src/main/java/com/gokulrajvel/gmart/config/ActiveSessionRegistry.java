package com.gokulrajvel.gmart.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe registry that keeps track of active user tokens/client-tokens.
 * Helps implement single-session concurrency control in a stateless JWT environment.
 * When a user logs in from a new device/browser, any existing session is automatically terminated.
 */
@Component
public class ActiveSessionRegistry {
    
    private final SimpMessagingTemplate messagingTemplate;
    // Map storing the mapping of username -> active clientToken (UUID string)
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();

    public ActiveSessionRegistry(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Registers a new client session for a user. If the user already has an active session on
     * a different browser or system, that previous session is invalidated via WebSocket broadcast.
     *
     * @param username    the name of the user logging in
     * @param clientToken the newly created UUID client token
     */
    public void registerSession(String username, String clientToken) {
        if (username == null || clientToken == null) {
            return;
        }
        
        String oldClientToken = activeSessions.put(username, clientToken);
        
        // If an old session existed and represents a different clientToken, broadcast logout
        if (oldClientToken != null && !oldClientToken.equals(clientToken)) {
            try {
                Map<String, String> logoutPayload = new HashMap<>();
                logoutPayload.put("action", "logout");
                logoutPayload.put("exceptClientToken", clientToken);
                messagingTemplate.convertAndSendToUser(username, "/queue/notifications", logoutPayload);
            } catch (Exception e) {
                System.err.println("Failed to broadcast WebSocket logout notification: " + e.getMessage());
            }
        }
    }

    /**
     * Validates whether a given clientToken is currently active for the user.
     */
    public boolean isSessionActive(String username, String clientToken) {
        if (username == null || clientToken == null) {
            return false;
        }
        String activeToken = activeSessions.get(username);
        return activeToken == null || activeToken.equals(clientToken);
    }

    /**
     * Removes a user's session from the registry.
     *
     * @param username the user to remove
     */
    public void removeSession(String username) {
        if (username != null) {
            activeSessions.remove(username);
        }
    }
}

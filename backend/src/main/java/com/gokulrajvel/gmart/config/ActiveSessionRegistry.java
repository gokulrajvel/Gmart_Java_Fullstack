package com.gokulrajvel.gmart.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Registry that keeps track of active user tokens in Redis.
 * Helps implement single-session concurrency control in a stateless JWT environment.
 * When a user logs in from a new device/browser, any existing session is automatically terminated.
 */
@Component
public class ActiveSessionRegistry {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;

    // Redis prefix for active user session keys
    private static final String REDIS_PREFIX = "gmart:session:";

    public ActiveSessionRegistry(SimpMessagingTemplate messagingTemplate, StringRedisTemplate redisTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Registers a new client session for a user in Redis. If the user already has an active session on
     * a different browser or system, that previous session is invalidated via WebSocket broadcast.
     *
     * @param username    the name of the user logging in
     * @param clientToken the newly created UUID client token
     */
    public void registerSession(String username, String clientToken) {
        if (username == null || clientToken == null) {
            return;
        }
        
        String key = REDIS_PREFIX + username;
        String oldClientToken = redisTemplate.opsForValue().get(key);
        
        // Save the active clientToken in Redis, expiring in 30 days
        redisTemplate.opsForValue().set(key, clientToken, 30, TimeUnit.DAYS);
        
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
     * Validates whether a given clientToken is currently active for the user in Redis.
     */
    public boolean isSessionActive(String username, String clientToken) {
        if (username == null || clientToken == null) {
            return false;
        }
        String key = REDIS_PREFIX + username;
        String activeToken = redisTemplate.opsForValue().get(key);
        return activeToken == null || activeToken.equals(clientToken);
    }

    /**
     * Removes a user's session from the registry (invalidates it in Redis).
     *
     * @param username the user to remove
     */
    public void removeSession(String username) {
        if (username != null) {
            redisTemplate.delete(REDIS_PREFIX + username);
        }
    }
}

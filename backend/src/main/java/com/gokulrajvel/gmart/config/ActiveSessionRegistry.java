package com.gokulrajvel.gmart.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Registry that keeps track of active user tokens.
 * Uses Redis as the primary store for distributed session concurrency control,
 * but falls back gracefully to an in-memory ConcurrentHashMap if Redis is offline/unavailable.
 */
@Component
public class ActiveSessionRegistry {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;

    // Redis prefix for active user session keys
    private static final String REDIS_PREFIX = "gmart:session:";

    // Fallback in-memory map used when Redis is offline
    private final Map<String, String> fallbackSessions = new ConcurrentHashMap<>();
    private boolean isRedisAvailable = true;

    public ActiveSessionRegistry(SimpMessagingTemplate messagingTemplate, StringRedisTemplate redisTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        checkRedisAvailability();
    }

    private void checkRedisAvailability() {
        try {
            // Test connection factory
            redisTemplate.getConnectionFactory().getConnection().ping();
            isRedisAvailable = true;
            System.out.println("Redis is ONLINE. Session registry will use Redis.");
        } catch (Exception e) {
            isRedisAvailable = false;
            System.err.println("WARNING: Redis is OFFLINE. Falling back to in-memory session registry. Error: " + e.getMessage());
        }
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
        
        String oldClientToken = null;

        if (isRedisAvailable) {
            try {
                String key = REDIS_PREFIX + username;
                oldClientToken = redisTemplate.opsForValue().get(key);
                redisTemplate.opsForValue().set(key, clientToken, 30, TimeUnit.DAYS);
            } catch (Exception e) {
                System.err.println("Redis communication failed during registerSession. Falling back to memory. Error: " + e.getMessage());
                isRedisAvailable = false; // Disable Redis for subsequent calls
                oldClientToken = fallbackSessions.put(username, clientToken);
            }
        } else {
            oldClientToken = fallbackSessions.put(username, clientToken);
        }
        
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

        if (isRedisAvailable) {
            try {
                String key = REDIS_PREFIX + username;
                String activeToken = redisTemplate.opsForValue().get(key);
                return activeToken == null || activeToken.equals(clientToken);
            } catch (Exception e) {
                System.err.println("Redis communication failed during isSessionActive. Falling back to memory. Error: " + e.getMessage());
                isRedisAvailable = false; // Disable Redis for subsequent calls
                String activeToken = fallbackSessions.get(username);
                return activeToken == null || activeToken.equals(clientToken);
            }
        } else {
            String activeToken = fallbackSessions.get(username);
            return activeToken == null || activeToken.equals(clientToken);
        }
    }

    /**
     * Removes a user's session from the registry.
     *
     * @param username the user to remove
     */
    public void removeSession(String username) {
        if (username == null) {
            return;
        }

        if (isRedisAvailable) {
            try {
                redisTemplate.delete(REDIS_PREFIX + username);
            } catch (Exception e) {
                System.err.println("Redis communication failed during removeSession. Falling back to memory. Error: " + e.getMessage());
                isRedisAvailable = false; // Disable Redis for subsequent calls
                fallbackSessions.remove(username);
            }
        } else {
            fallbackSessions.remove(username);
        }
    }
}

package com.gokulrajvel.gmart.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Thread-safe registry that keeps track of active user sessions.
 * Helps implement single-session concurrency control. When a user logs in 
 * from a new device/browser, any existing active session is automatically terminated.
 */
@Component
public class ActiveSessionRegistry {
    
    // Concurrent map storing the mapping of username -> active HttpSession
    private final Map<String, HttpSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Registers a new session for a user. If the user already has an active session on
     * a different browser or system, that previous session is invalidated.
     *
     * @param username the name of the user logging in
     * @param session  the newly created HTTP session
     */
    public void registerSession(String username, HttpSession session) {
        if (username == null || session == null) {
            return;
        }
        
        HttpSession oldSession = activeSessions.put(username, session);
        // If an old session existed and it represents a different session ID, invalidate it
        if (oldSession != null && !oldSession.getId().equals(session.getId())) {
            try {
                oldSession.invalidate();
            } catch (IllegalStateException e) {
                // Session was already invalidated or expired
            }
        }
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

    /**
     * Removes a specific session from the registry by its session object.
     * This is typically called from a session listener when Tomcat/Spring container 
     * destroys a session due to timeout or logout.
     *
     * @param session the session object to remove
     */
    public void removeSession(HttpSession session) {
        if (session != null) {
            activeSessions.values().removeIf(s -> s.getId().equals(session.getId()));
        }
    }
}

package com.gokulrajvel.gmart.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class ActiveSessionRegistry {
    // Map of username -> HttpSession
    private final Map<String, HttpSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Registers a new session for a user. If the user already has an active session on
     * a different browser or system, that previous session is invalidated.
     */
    public void registerSession(String username, HttpSession session) {
        if (username == null || session == null) {
            return;
        }
        
        HttpSession oldSession = activeSessions.put(username, session);
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
     */
    public void removeSession(String username) {
        if (username != null) {
            activeSessions.remove(username);
        }
    }

    /**
     * Removes a specific session from the registry by its session object.
     * This is typically called from a session listener.
     */
    public void removeSession(HttpSession session) {
        if (session != null) {
            activeSessions.values().removeIf(s -> s.getId().equals(session.getId()));
        }
    }
}

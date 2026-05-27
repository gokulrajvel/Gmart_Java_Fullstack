package com.gokulrajvel.gmart.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe registry that keeps track of active user sessions.
 * Helps implement single-session concurrency control. When a user logs in 
 * from a new device/browser, any existing active session is automatically terminated.
 */
@Component
public class ActiveSessionRegistry {
    
    private final SimpMessagingTemplate messagingTemplate;

    public ActiveSessionRegistry(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Wrapper class to store the HttpSession along with its ID as a String.
     * This avoids calling session.getId() on already-invalidated sessions,
     * which would throw an IllegalStateException.
     */
    public static class UserSession {
        private final String sessionId;
        private final HttpSession session;

        public UserSession(String sessionId, HttpSession session) {
            this.sessionId = sessionId;
            this.session = session;
        }

        public String getSessionId() {
            return sessionId;
        }

        public HttpSession getSession() {
            return session;
        }
    }

    // Map storing the mapping of username -> UserSession wrapper
    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();

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
        
        String newSessionId;
        try {
            newSessionId = session.getId();
        } catch (IllegalStateException e) {
            // New session is somehow already invalid
            return;
        }

        UserSession newUserSession = new UserSession(newSessionId, session);
        UserSession oldUserSession = activeSessions.put(username, newUserSession);
        
        // If an old session existed and it represents a different session ID, invalidate it
        if (oldUserSession != null && !oldUserSession.getSessionId().equals(newSessionId)) {
            // Retrieve the new clientToken that should be spared from the logout broadcast
            String newClientToken = (String) session.getAttribute("clientToken");

            // Broadcast a logout command to all active WebSocket sessions for this username,
            // telling them to log out unless their clientToken matches newClientToken.
            try {
                Map<String, String> logoutPayload = new HashMap<>();
                logoutPayload.put("action", "logout");
                logoutPayload.put("exceptClientToken", newClientToken);
                messagingTemplate.convertAndSendToUser(username, "/queue/notifications", logoutPayload);
            } catch (Exception e) {
                // If WebSocket broadcasting fails, log and fallback to standard HTTP session invalidation
                System.err.println("Failed to broadcast WebSocket logout notification: " + e.getMessage());
            }

            try {
                oldUserSession.getSession().invalidate();
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
        if (session == null) {
            return;
        }

        String targetSessionId;
        try {
            targetSessionId = session.getId();
        } catch (IllegalStateException e) {
            // If the session is already fully invalidated, we can't retrieve its ID.
            // Since it is already invalidated, we do not need to process it.
            return;
        }
        
        // Remove matching session IDs safely using the String sessionId in the wrapper
        activeSessions.values().removeIf(us -> us.getSessionId().equals(targetSessionId));
    }
}

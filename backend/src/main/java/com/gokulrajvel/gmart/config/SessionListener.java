package com.gokulrajvel.gmart.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

/**
 * Global HTTP Session Listener managed as a Spring Bean.
 * Automatically registered by Spring Boot to hook into the servlet container's
 * session lifecycle. It ensures that when sessions expire or are destroyed (e.g. on logout),
 * they are cleaned up from the ActiveSessionRegistry to prevent memory leaks.
 */
@Component
public class SessionListener implements HttpSessionListener {

    private final ActiveSessionRegistry sessionRegistry;

    /**
     * Constructs the session listener with the shared ActiveSessionRegistry bean.
     *
     * @param sessionRegistry the active session mapping manager
     */
    public SessionListener(ActiveSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // No action needed on creation
    }

    /**
     * Triggered automatically when an HTTP session is destroyed (invalidated or timed out).
     * Cleans up the invalidated session mapping from the registry.
     *
     * @param se the session lifecycle event payload
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // Remove the session from our active sessions map
        sessionRegistry.removeSession(se.getSession());
    }
}

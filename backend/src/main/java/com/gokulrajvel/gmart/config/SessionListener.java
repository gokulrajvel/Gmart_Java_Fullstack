package com.gokulrajvel.gmart.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

@Component
public class SessionListener implements HttpSessionListener {

    private final ActiveSessionRegistry sessionRegistry;

    public SessionListener(ActiveSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // No action needed on creation
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // Remove the session from our active sessions map
        sessionRegistry.removeSession(se.getSession());
    }
}

package com.gokulrajvel.gmart.controller;

import com.gokulrajvel.gmart.config.ActiveSessionRegistry;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * Controller handling user authentication requests.
 * Manages login validation, programmatic SecurityContext setup for stateful HTTP sessions,
 * and integration with the ActiveSessionRegistry to enforce single concurrent sessions.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final ActiveSessionRegistry activeSessionRegistry;

    /**
     * Constructs the authentication controller with required services.
     *
     * @param userService           service to authenticate user credentials
     * @param activeSessionRegistry registry to track single active session per user
     */
    public AuthController(UserService userService, ActiveSessionRegistry activeSessionRegistry) {
        this.userService = userService;
        this.activeSessionRegistry = activeSessionRegistry;
    }

    /**
     * Handles POST requests for user login.
     * Validates credentials, sets up programmatic Spring Security authentication tokens,
     * links the authentication with an HttpSession, and registers the session to prevent concurrent logins.
     *
     * @param loginRequest the credentials payload containing username and password
     * @param request      the HTTP request wrapper used to retrieve/create sessions
     * @return 200 OK with authenticated user profile details, or 401 Unauthorized on invalid credentials
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest, HttpServletRequest request) {
        User authenticatedUser = userService.authenticate(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        if (authenticatedUser != null) {
            // Programmatically set standard security context for stateful sessions
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    authenticatedUser.getUsername(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + authenticatedUser.getRole().name()))
            );
            
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authenticationToken);
            
            // Link authentication context with HttpSession
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            // Register session in our active session registry (will invalidate previous session of the same user)
            activeSessionRegistry.registerSession(authenticatedUser.getUsername(), session);

            return ResponseEntity.ok(authenticatedUser);
        } else {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
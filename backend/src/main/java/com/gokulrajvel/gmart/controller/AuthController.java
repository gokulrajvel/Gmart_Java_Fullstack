package com.gokulrajvel.gmart.controller;

import com.gokulrajvel.gmart.config.ActiveSessionRegistry;
import com.gokulrajvel.gmart.config.JwtTokenProvider;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling user authentication requests.
 * Manages login validation, JWT generation, and integration with 
 * the ActiveSessionRegistry to enforce single concurrent sessions.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final ActiveSessionRegistry activeSessionRegistry;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Constructs the authentication controller with required services.
     *
     * @param userService           service to authenticate user credentials
     * @param activeSessionRegistry registry to track single active session per user
     * @param jwtTokenProvider      provider to generate and manage JWT tokens
     */
    public AuthController(UserService userService, 
                          ActiveSessionRegistry activeSessionRegistry, 
                          JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.activeSessionRegistry = activeSessionRegistry;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Handles POST requests for user login.
     * Validates credentials, generates a new JWT token, and registers 
     * the session identifier to prevent concurrent logins.
     *
     * @param loginRequest the credentials payload containing username and password
     * @return 200 OK with authenticated user profile details and JWT, or 401 Unauthorized on invalid credentials
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User authenticatedUser = userService.authenticate(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        if (authenticatedUser != null) {
            // Generate a unique client token UUID to identify this session
            String clientTokenUuid = java.util.UUID.randomUUID().toString();

            // Register this session (will broadcast logout event if user is logged in elsewhere)
            activeSessionRegistry.registerSession(authenticatedUser.getUsername(), clientTokenUuid);

            // Generate stateless JWT containing role, userId, and clientTokenUuid
            String jwt = jwtTokenProvider.generateToken(
                    authenticatedUser.getUsername(),
                    authenticatedUser.getRole().name(),
                    (long) authenticatedUser.getId(),
                    clientTokenUuid
            );

            // Return JWT as the clientToken payload
            return ResponseEntity.ok(new LoginResponse(authenticatedUser, jwt));
        } else {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    /**
     * Endpoint to silently verify if the current session token is still valid.
     * If the JWT token is invalid, expired, or has been blacklisted, 
     * Spring Security will intercept the request and return 401 Unauthorized beforehand.
     *
     * @return 200 OK if token is valid and active
     */
    @GetMapping("/status")
    public ResponseEntity<?> checkStatus() {
        return ResponseEntity.ok().build();
    }

    /**
     * DTO containing user data and JWT token returned upon successful authentication.
     */
    public static class LoginResponse {
        private final User user;
        private final String clientToken; // Named clientToken to maintain compatibility with existing client-side code

        public LoginResponse(User user, String clientToken) {
            this.user = user;
            this.clientToken = clientToken;
        }

        public User getUser() {
            return user;
        }

        public String getClientToken() {
            return clientToken;
        }
    }
}
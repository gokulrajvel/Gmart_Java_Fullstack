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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final ActiveSessionRegistry activeSessionRegistry;

    public AuthController(UserService userService, ActiveSessionRegistry activeSessionRegistry) {
        this.userService = userService;
        this.activeSessionRegistry = activeSessionRegistry;
    }

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
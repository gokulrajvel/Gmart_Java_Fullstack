package com.gokulrajvel.gmart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Configuration class to set up Web Security parameters for GMart.
 * Enables web security, sets up CORS rules, overrides Spring Security's password hashing
 * mechanism to handle plain-text migrations safely, and configures URL access controls.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Bean declaration for Password Hashing using BCrypt.
     * Custom implementation handles legacy plain-text passwords by checking hashing formats.
     *
     * @return custom PasswordEncoder that defaults to BCrypt but supports plain-text comparisons
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) {
                    return false;
                }
                // Check if stored password is BCrypt hashed (prefix matches standard BCrypt identifier)
                if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
                    return bcrypt.matches(rawPassword, encodedPassword);
                }
                // Fallback for migrating unhashed legacy developer passwords
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    /**
     * Primary Security Configuration Filter Chain.
     * Defines path authorization limits, registers stateful session details, 
     * and sets up logout behaviors like invalidating session registries.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS policies and disable CSRF for REST interactions
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            
            // Path-based Route Authorization Controls
            .authorizeHttpRequests(auth -> auth
                // Allow public access to all landing/assets and UI pages of SPA frontend
                .requestMatchers("/", "/index.html", "/dashboard.html", "/css/**", "/js/**", "/assets/**", "/favicon.ico", "/error").permitAll()
                // Permit anyone to hit login endpoints
                .requestMatchers("/login", "/api/auth/login").permitAll()
                // Require authenticated sessions for WebSockets and general API requests
                .requestMatchers("/ws/**").authenticated()
                .anyRequest().authenticated()
            )
            
            // Stateful session settings utilizing standard session cookies
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            
            // Logout definitions (invalidates HTTP session, clears Security Context, and removes JSESSIONID cookie)
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    /**
     * CORS policies to permit cross-origin access during development.
     * Maps and handles allowed HTTP headers, origins, methods, and credentials.
     *
     * @return Configured CorsConfigurationSource instance
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow requests from all origins (useful during frontend/backend decoupled dev environments)
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        // Expose Set-Cookie header so frontend clients receive and store JSESSIONID correctly
        configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

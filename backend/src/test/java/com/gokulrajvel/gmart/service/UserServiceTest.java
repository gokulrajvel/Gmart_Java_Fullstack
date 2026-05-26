package com.gokulrajvel.gmart.service;

import com.gokulrajvel.gmart.data.Role;
import com.gokulrajvel.gmart.data.dto.User;
import com.gokulrajvel.gmart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserService userService;
    private UserRepository mockUserRepository;
    private PasswordEncoder mockPasswordEncoder;

    @BeforeEach
    public void setUp() {
        mockUserRepository = mock(UserRepository.class);
        mockPasswordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(mockUserRepository, mockPasswordEncoder);
    }

    @Test
    public void testAuthenticate_Success() {
        User user = new User(1, "admin", "admin123", Role.ADMIN);
        when(mockUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(mockPasswordEncoder.matches("admin123", "admin123")).thenReturn(true);
        
        User authenticated = userService.authenticate("admin", "admin123");
        assertNotNull(authenticated);
        assertEquals("admin", authenticated.getUsername());
        assertEquals(Role.ADMIN, authenticated.getRole());
    }

    @Test
    public void testAuthenticate_WrongPassword() {
        User user = new User(1, "admin", "admin123", Role.ADMIN);
        when(mockUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(mockPasswordEncoder.matches("wrong_pass", "admin123")).thenReturn(false);
        
        User authenticated = userService.authenticate("admin", "wrong_pass");
        assertNull(authenticated);
    }

    @Test
    public void testAuthenticate_UserNotFound() {
        when(mockUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        User authenticated = userService.authenticate("nonexistent", "some_pass");
        assertNull(authenticated);
    }

    @Test
    public void testAuthenticate_NullUsernameOrPassword() {
        // Mock findByUsername behavior when receiving null input
        when(mockUserRepository.findByUsername(null)).thenReturn(Optional.empty());
        
        User authenticatedNullUser = userService.authenticate(null, "some_pass");
        assertNull(authenticatedNullUser);
        
        User user = new User(1, "admin", "admin123", Role.ADMIN);
        when(mockUserRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(mockPasswordEncoder.matches(null, "admin123")).thenReturn(false);
        
        User authenticatedNullPass = userService.authenticate("admin", null);
        assertNull(authenticatedNullPass);
    }
}

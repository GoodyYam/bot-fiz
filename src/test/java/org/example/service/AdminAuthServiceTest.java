package org.example.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminAuthServiceTest {

    @Test
    void testEmptyOrNullConfigDeniesAll() {
        AdminAuthService nullService = new AdminAuthService(null);
        assertFalse(nullService.isAdmin(12345L));

        AdminAuthService emptyService = new AdminAuthService("   ");
        assertFalse(emptyService.isAdmin(12345L));
    }

    @Test
    void testValidConfigAllowsAdmins() {
        AdminAuthService service = new AdminAuthService("111, 222");

        assertTrue(service.isAdmin(111L));
        assertTrue(service.isAdmin(222L));
        assertFalse(service.isAdmin(333L)); // Чужой юзер
    }

    @Test
    void testInvalidConfigThrowsException() {
        assertThrows(IllegalStateException.class, () -> new AdminAuthService("111, abc"));
    }
}
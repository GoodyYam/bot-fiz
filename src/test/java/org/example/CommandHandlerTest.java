package org.example;

import org.example.service.AdminAuthService;
import org.example.service.DefaultSystemMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandHandlerTest  {
    private CommandHandler handler;
    private final long ADMIN_ID = 100L;
    private final long GUEST_ID = 999L;

    @BeforeEach
    void setUp() {
        AdminAuthService stubAuth = new AdminAuthService("100");
        handler = new CommandHandler(new DefaultSystemMetricsService(), stubAuth);
    }

    @Test
    void testAuthorCommand() {
        String response = handler.handle("/author", GUEST_ID);
        assertTrue(response.contains("Авторы проекта"));
    }

    @Test
    void testAboutCommand() {
        String response = handler.handle("/about", GUEST_ID);
        assertTrue(response.contains("Бот для удаленного мониторинга"));
    }

    @Test
    void testHelpWithoutArgsShowsAllCommands() {
        String response = handler.handle("/help", GUEST_ID);
        assertTrue(response.contains("/author"));
        assertTrue(response.contains("/about"));
        assertTrue(response.contains("/help"));
    }

    @Test
    void testHelpWithValidArg() {
        String response = handler.handle("/help author", GUEST_ID);
        assertTrue(response.contains("Выводит информацию об авторах"));
    }

    @Test
    void testHelpWithInvalidArg() {
        String response = handler.handle("/help unknown", GUEST_ID);
        assertTrue(response.contains("не найдена"));
    }

    @Test
    void testPrivateCommandBlockedForGuest() {
        String response = handler.handle("/cpu", GUEST_ID);
        assertTrue(response.contains("Отказ в доступе"));
        assertFalse(response.contains("Доступно потоков:"));
    }

    @Test
    void testPrivateCommandAccessibleByAdmin() {
        String response = handler.handle("/cpu", ADMIN_ID);
        assertFalse(response.contains("Отказ в доступе"));
        assertTrue(response.contains("Доступно потоков:"));
    }
}
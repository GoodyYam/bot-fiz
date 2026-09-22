package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandHandlerTest {
    private CommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CommandHandler();
    }

    @Test
    void testAuthorCommand() {
        String response = handler.handle("/author");
        assertTrue(response.contains("Авторы проекта"));
    }

    @Test
    void testAboutCommand() {
        String response = handler.handle("/about");
        assertTrue(response.contains("Бот для удаленного мониторинга и администрирования серверов. Предоставляет сводку по утилизации ресурсов (CPU, RAM, диск), проверяет доступность сервисов и отправляет экстренные алерты при сбоях."));
    }

    @Test
    void testHelpWithoutArgsShowsAllCommands() {
        String response = handler.handle("/help");
        assertTrue(response.contains("/author"));
        assertTrue(response.contains("/about"));
        assertTrue(response.contains("/help"));
    }

    @Test
    void testHelpWithValidArg() {
        String response = handler.handle("/help author");
        assertTrue(response.contains("Выводит информацию об авторах"));
    }

    @Test
    void testHelpWithInvalidArg() {
        String response = handler.handle("/help unknown");
        assertTrue(response.contains("не найдена"));
    }
}
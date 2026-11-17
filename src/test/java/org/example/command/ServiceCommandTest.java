package org.example.command;

import org.example.service.ServiceCheckService;
import org.example.service.model.ServiceStatusResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceCommandTest {

    private ServiceCheckService stubService;
    private ServiceCommand command;

    @BeforeEach
    void setUp() {
        stubService = new ServiceCheckService() {
            @Override
            public ServiceStatusResult checkServiceStatus(String serviceName) {
                if ("nginx".equals(serviceName)) {
                    return new ServiceStatusResult(true, "active", null);
                }
                return new ServiceStatusResult(
                        false,
                        "inactive",
                        "Служба находится в состоянии 'inactive' (код возврата: 3)"
                );
            }
        };
        command = new ServiceCommand(stubService);
    }

    @Test
    void testMetadata() {
        assertEquals("service", command.getName());
        assertFalse(command.getDescription().isEmpty());
    }

    @Test
    void testEmptyArguments() {
        assertTrue(command.execute(new String[0]).contains("Использование: /service <name>"));
        assertTrue(command.execute(new String[]{"   "}).contains("Использование: /service <name>"));
    }

    @Test
    void testCommandInjectionBlocked() {
        String multiArgInjection = command.execute(new String[]{"nginx;", "rm", "-rf", "/"});
        assertTrue(multiArgInjection.contains("Ошибка безопасности"));

        assertTrue(command.execute(new String[]{"nginx;rm"}).contains("Ошибка безопасности"));
        assertTrue(command.execute(new String[]{"nginx&&whoami"}).contains("Ошибка безопасности"));
        assertTrue(command.execute(new String[]{"$(reboot)"}).contains("Ошибка безопасности"));
    }

    @Test
    void testServiceNotInWhitelist() {
        String output = command.execute(new String[]{"apache2"});
        assertTrue(output.contains("не входит в белый список"));
        assertTrue(output.contains("nginx"));
    }

    @Test
    void testActiveServiceOutput() {
        String output = command.execute(new String[]{"nginx"});
        assertTrue(output.contains("Служба nginx"));
        assertTrue(output.contains("Активна (active) ✅"));
    }

    @Test
    void testInactiveServiceOutput() {
        String output = command.execute(new String[]{"docker"});
        assertTrue(output.contains("Служба docker"));
        assertTrue(output.contains("Неактивна (inactive) ❌"));
        assertTrue(output.contains("код возврата: 3"));
    }
}
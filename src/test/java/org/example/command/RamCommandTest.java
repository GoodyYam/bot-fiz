package org.example.command;

import org.example.service.DefaultSystemMetricsService;
import org.example.service.SystemMetricsService;
import org.example.service.model.RamMetrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RamCommandTest {

    private static final long ONE_GB = 1024L * 1024L * 1024L;

    private final SystemMetricsService stubMetricsService = new SystemMetricsService() {
        @Override
        public RamMetrics getRamMetrics() {
            return new RamMetrics(
                    16 * ONE_GB,
                    4 * ONE_GB,
                    8 * ONE_GB,
                    6 * ONE_GB
            );
        }

        @Override
        public double getCpuLoadPercent() {
            return 50.0;
        }

        @Override
        public int getAvailableProcessors() {
            return 8;
        }
    };

    @Test
    void testRamMetricsCalculations() {
        RamMetrics metrics = stubMetricsService.getRamMetrics();

        assertEquals(12 * ONE_GB, metrics.usedPhysicalBytes());
        assertEquals(75.0, metrics.physicalUsagePercent(), 0.01);
        assertEquals(2 * ONE_GB, metrics.usedSwapBytes());
        assertEquals(25.0, metrics.swapUsagePercent(), 0.01);
    }

    @Test
    void testRamCommandDefaultOutputInGb() {
        RamCommand command = new RamCommand(stubMetricsService);
        String result = command.execute(new String[]{});

        assertEquals("ram", command.getName());
        assertFalse(command.getDescription().isBlank());

        assertTrue(result.contains("75.0%"));
        assertTrue(result.contains("Использовано: 12.00 ГБ"));
        assertTrue(result.contains("Свободно: 4.00 ГБ"));
        assertTrue(result.contains("Всего: 16.00 ГБ"));

        assertTrue(result.contains("25.0%"));
        assertTrue(result.contains("Использовано: 2.00 ГБ"));
    }

    @Test
    void testRamCommandForceMbOutput() {
        RamCommand command = new RamCommand(stubMetricsService);
        String result = command.execute(new String[]{"mb"});

        assertTrue(result.contains("12288.00 МБ"));
        assertTrue(result.contains("16384.00 МБ"));
    }

    @Test
    void testRamCommandInvalidArgument() {
        RamCommand command = new RamCommand(stubMetricsService);
        String result = command.execute(new String[]{"invalid_arg"});

        assertTrue(result.contains("Неизвестный аргумент"));
    }

    @Test
    void testDefaultSystemMetricsServiceReturnsValidOsData() {
        DefaultSystemMetricsService realService = new DefaultSystemMetricsService();
        RamMetrics realMetrics = realService.getRamMetrics();

        assertTrue(realMetrics.totalPhysicalBytes() > 0);
        assertTrue(realMetrics.freePhysicalBytes() >= 0);
        assertTrue(realMetrics.freePhysicalBytes() <= realMetrics.totalPhysicalBytes());
    }
}

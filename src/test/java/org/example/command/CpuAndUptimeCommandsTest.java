package org.example.command;

import org.example.service.CpuMonitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpuAndUptimeCommandsTest {

    private CpuMonitorService stubService;

    @BeforeEach
    void setUp() {
        stubService = new CpuMonitorService() {
            @Override
            public double getSystemCpuLoadPercent() {
                return 37.5;
            }

            @Override
            public double getProcessCpuLoadPercent() {
                return 4.2;
            }

            @Override
            public double getSystemLoadAverage() {
                return 1.15;
            }

            @Override
            public int getAvailableProcessors() {
                return 8;
            }

            @Override
            public long getJvmUptimeMillis() {
                return 3_661_000L; // 1 ч. 1 мин. 1 сек.
            }

            @Override
            public long getOsUptimeMillis() {
                return 86_400_000L; // 1 дн. 0 ч. 0 мин. 0 сек.
            }
        };
    }

    @Test
    void testCpuCommand() {
        CpuCommand command = new CpuCommand(stubService);

        assertEquals("cpu", command.getName());
        assertFalse(command.getDescription().isEmpty());

        String output = command.execute(new String[0]);
        assertTrue(output.contains("Доступно ядер: 8"));
        assertTrue(output.contains("37.5%"));
        assertTrue(output.contains("4.2%"));
        assertTrue(output.contains("1.15"));
    }

    @Test
    void testUptimeCommand() {
        UptimeCommand command = new UptimeCommand(stubService);

        assertEquals("uptime", command.getName());
        assertFalse(command.getDescription().isEmpty());

        String output = command.execute(new String[0]);
        assertTrue(output.contains("1 ч. 1 мин. 1 сек."));
        assertTrue(output.contains("1 дн. 0 ч. 0 мин. 0 сек."));
    }
}
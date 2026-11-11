package org.example.command;

import org.example.service.CpuMonitorService;
import org.example.service.SystemMetricsService;
import org.example.service.model.RamMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusCommandTest {

    private SystemMetricsService stubMetricsService;
    private CpuMonitorService stubCpuService;

    @BeforeEach
    void setUp() {
        stubMetricsService = new SystemMetricsService() {
            @Override
            public RamMetrics getRamMetrics() {
                long totalRam = 16L * 1024 * 1024 * 1024; // 16 ГБ
                long freeRam = 8L * 1024 * 1024 * 1024;   // 8 ГБ свободно (50% занято)
                long totalSwap = 4L * 1024 * 1024 * 1024; // 4 ГБ
                long freeSwap = 3L * 1024 * 1024 * 1024;  // 3 ГБ свободно (25% занято)
                return new RamMetrics(totalRam, freeRam, totalSwap, freeSwap);
            }

            @Override
            public double getCpuLoadPercent() {
                return 40.0;
            }

            @Override
            public int getAvailableProcessors() {
                return 12;
            }
        };

        stubCpuService = new CpuMonitorService() {
            @Override
            public double getSystemLoadAverage() {
                return 1.25;
            }

            @Override
            public long getJvmUptimeMillis() {
                return 600_000L; // 10 мин. 0 сек.
            }

            @Override
            public long getOsUptimeMillis() {
                return 7_200_000L; // 2 ч. 0 мин. 0 сек.
            }
        };
    }

    @Test
    void testStatusCommandMetadata() {
        StatusCommand command = new StatusCommand(stubMetricsService, stubCpuService);
        assertEquals("status", command.getName());
        assertFalse(command.getDescription().isEmpty());
    }

    @Test
    void testStatusCommandExecuteIncludesAllMetrics() {
        StatusCommand command = new StatusCommand(stubMetricsService, stubCpuService);
        String report = command.execute(new String[0]);

        assertTrue(report.contains("Сводный статус сервера"));
        assertTrue(report.contains("12 ядер"));
        assertTrue(report.contains("[████░░░░░░] 40.0%"));
        assertTrue(report.contains("1.25"));
        assertTrue(report.contains("[█████░░░░░] 50.0%"));
        assertTrue(report.contains("8.00 ГБ / 16.00 ГБ"));
        assertTrue(report.contains("10 мин. 0 сек."));
        assertTrue(report.contains("2 ч. 0 мин. 0 сек."));
    }

    @Test
    void testBuildProgressBarEdgeCases() {
        assertEquals("[░░░░░░░░░░] 0.0%", StatusCommand.buildProgressBar(0.0));
        assertEquals("[█████░░░░░] 50.0%", StatusCommand.buildProgressBar(50.0));
        assertEquals("[██████████] 100.0%", StatusCommand.buildProgressBar(100.0));
        assertEquals("[░░░░░░░░░░] Н/Д", StatusCommand.buildProgressBar(-1.0));
    }

    @Test
    void testFormatBytes() {
        assertEquals("512.0 МБ", StatusCommand.formatBytes(512L * 1024 * 1024));
        assertEquals("2.00 ГБ", StatusCommand.formatBytes(2L * 1024 * 1024 * 1024));
    }
}
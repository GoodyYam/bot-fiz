package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CpuMonitorServiceTest {

    private CpuMonitorService service;

    @BeforeEach
    void setUp() {
        service = new CpuMonitorService();
    }

    @Test
    void testNormalizeLoadPercent_ValidValues() {
        assertEquals(0.0, service.normalizeLoadPercent(0.0), 0.001);
        assertEquals(42.5, service.normalizeLoadPercent(0.425), 0.001);
        assertEquals(100.0, service.normalizeLoadPercent(1.0), 0.001);
        // Если JVM вернула значение чуть больше 1.0 из-за погрешности, ограничиваем 100.0
        assertEquals(100.0, service.normalizeLoadPercent(1.05), 0.001);
    }

    @Test
    void testNormalizeLoadPercent_InvalidOrUnavailableValues() {
        assertEquals(-1.0, service.normalizeLoadPercent(-1.0));
        assertEquals(-1.0, service.normalizeLoadPercent(-0.01));
        assertEquals(-1.0, service.normalizeLoadPercent(Double.NaN));
    }

    @Test
    void testFormatDuration_VariousIntervals() {
        assertEquals("Н/Д", service.formatDuration(-100L));
        assertEquals("0 сек.", service.formatDuration(0L));
        assertEquals("0 сек.", service.formatDuration(999L));
        assertEquals("45 сек.", service.formatDuration(45_000L));
        assertEquals("2 мин. 5 сек.", service.formatDuration(125_000L));
        assertEquals("1 ч. 0 мин. 0 сек.", service.formatDuration(3_600_000L));
        assertEquals("1 дн. 2 ч. 3 мин. 4 сек.", service.formatDuration(93_784_000L));
    }

    @Test
    void testReadLinuxProcUptimeMillis_ValidFile(@TempDir Path tempDir) throws IOException {
        Path fakeProcUptime = tempDir.resolve("uptime");
        Files.writeString(fakeProcUptime, "12345.67 98765.43\n");

        long uptimeMillis = service.readLinuxProcUptimeMillis(fakeProcUptime);
        assertEquals(12_345_670L, uptimeMillis);
    }

    @Test
    void testReadLinuxProcUptimeMillis_InvalidOrMissingFile(@TempDir Path tempDir) throws IOException {
        Path nonExistent = tempDir.resolve("missing_uptime");
        assertEquals(-1L, service.readLinuxProcUptimeMillis(nonExistent));

        Path corrupted = tempDir.resolve("corrupted_uptime");
        Files.writeString(corrupted, "not_a_number 123.45");
        assertEquals(-1L, service.readLinuxProcUptimeMillis(corrupted));
    }

    @Test
    void testRealJvmMetricsAreValid() {
        assertTrue(service.getAvailableProcessors() > 0, "Количество ядер должно быть больше 0");
        assertTrue(service.getJvmUptimeMillis() >= 0L, "Аптайм JVM должен быть неотрицательным");

        String cpuReport = service.getCpuStatusReport();
        assertNotNull(cpuReport);
        assertTrue(cpuReport.contains("Доступно ядер:"));

        String uptimeReport = service.getUptimeReport();
        assertNotNull(uptimeReport);
        assertTrue(uptimeReport.contains("Аптайм бота (JVM):"));
    }
}
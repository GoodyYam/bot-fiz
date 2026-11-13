package org.example.command;

import org.example.service.CpuMonitorService;
import org.example.service.DefaultSystemMetricsService;
import org.example.service.SystemMetricsService;
import org.example.service.model.RamMetrics;

import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class StatusCommand implements Command {

    private final SystemMetricsService metricsService;
    private final CpuMonitorService cpuMonitorService;

    public StatusCommand() {
        this(new DefaultSystemMetricsService(), new CpuMonitorService());
    }

    public StatusCommand(SystemMetricsService metricsService, CpuMonitorService cpuMonitorService) {
        this.metricsService = metricsService;
        this.cpuMonitorService = cpuMonitorService;
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "Показать сводный статус сервера (CPU, RAM, Disk, Uptime)";
    }

    @Override
    public String execute(String[] args) {
        double cpuLoad = metricsService.getCpuLoadPercent();
        if (cpuLoad < 0) {
            cpuLoad = cpuMonitorService.getSystemCpuLoadPercent();
        }
        int cores = metricsService.getAvailableProcessors();
        double loadAvg = cpuMonitorService.getSystemLoadAverage();

        RamMetrics ram = metricsService.getRamMetrics();
        long jvmUptime = cpuMonitorService.getJvmUptimeMillis();
        long osUptime = cpuMonitorService.getOsUptimeMillis();

        StringBuilder sb = new StringBuilder("📊 Сводный статус сервера:\n\n");

        // 1. Блок CPU
        sb.append("💻 CPU (").append(cores).append(" потоков):\n")
                .append("• Нагрузка: ").append(buildProgressBar(cpuLoad)).append("\n");
        if (loadAvg >= 0) {
            sb.append("• Load Average: ").append(String.format(Locale.US, "%.2f", loadAvg)).append("\n");
        }

        // 2. Блок RAM и Swap
        if (ram != null) {
            sb.append("\n🧠 Память (RAM):\n")
                    .append("• ОЗУ: ").append(buildProgressBar(ram.physicalUsagePercent()))
                    .append(" (").append(formatBytes(ram.usedPhysicalBytes()))
                    .append(" / ").append(formatBytes(ram.totalPhysicalBytes())).append(")\n");

            if (ram.totalSwapBytes() > 0) {
                sb.append("• Swap: ").append(buildProgressBar(ram.swapUsagePercent()))
                        .append(" (").append(formatBytes(ram.usedSwapBytes()))
                        .append(" / ").append(formatBytes(ram.totalSwapBytes())).append(")\n");
            }
        }

        // 3. Блок Диска
        sb.append("\n💾 Диск:\n").append(getDiskSummary()).append("\n");

        // 4. Блок Uptime
        sb.append("\n⏱ Время работы:\n")
                .append("• JVM (бот): ").append(cpuMonitorService.formatDuration(jvmUptime));
        if (osUptime >= 0) {
            sb.append("\n• ОС (сервер): ").append(cpuMonitorService.formatDuration(osUptime));
        }

        return sb.toString();
    }

    private String getDiskSummary() {
        try {
            FileStore store = Files.getFileStore(Path.of(".").toAbsolutePath());
            long total = store.getTotalSpace();
            long usable = store.getUsableSpace();
            long used = Math.max(0L, total - usable);
            double percent = total > 0 ? (used * 100.0) / total : 0.0;
            return "• Основной раздел: " + buildProgressBar(percent) +
                    " (" + formatBytes(used) + " / " + formatBytes(total) + ")";
        } catch (Exception e) {
            return "• Основной раздел: Н/Д";
        }
    }

    public static String buildProgressBar(double percent) {
        if (Double.isNaN(percent) || percent < 0.0) {
            return "[░░░░░░░░░░] Н/Д";
        }
        double clamped = Math.min(100.0, Math.max(0.0, percent));
        int filled = (int) Math.round(clamped / 10.0);
        return "[" + "█".repeat(filled) + "░".repeat(10 - filled) + "] " +
                String.format(Locale.US, "%.1f%%", clamped);
    }

    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "0 Б";
        }
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);
        if (gb >= 1.0) {
            return String.format(Locale.US, "%.2f ГБ", gb);
        }
        double mb = bytes / (1024.0 * 1024.0);
        return String.format(Locale.US, "%.1f МБ", mb);
    }
}
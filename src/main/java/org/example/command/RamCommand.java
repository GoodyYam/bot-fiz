package org.example.command;

import org.example.service.SystemMetricsService;
import org.example.service.model.RamMetrics;

import java.util.Locale;

public class RamCommand implements Command {

    private static final double BYTES_IN_MB = 1024.0 * 1024.0;
    private static final double BYTES_IN_GB = 1024.0 * 1024.0 * 1024.0;
    private static final int PROGRESS_BAR_LENGTH = 10;

    private final SystemMetricsService metricsService;

    public RamCommand(SystemMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public String getName() {
        return "ram";
    }

    @Override
    public String getDescription() {
        return "Показать состояние оперативной памяти и файла подкачки (Swap)";
    }

    @Override
    public String execute(String[] args) {
        String unitMode = "";
        if (args != null && args.length > 0 && args[0] != null) {
            unitMode = args[0].trim().toLowerCase(Locale.ROOT);
        }

        if ((args != null && args.length > 1) || (!unitMode.isEmpty() && !unitMode.equals("mb") && !unitMode.equals("gb"))) {
            return "Неизвестный аргумент для /ram. Используйте: /ram, /ram mb или /ram gb";
        }

        RamMetrics metrics = metricsService.getRamMetrics();

        StringBuilder sb = new StringBuilder();
        sb.append("📊 Состояние памяти сервера:\n\n");

        // Блок физической памяти (RAM)
        sb.append("🔹 Оперативная память (RAM):\n");
        sb.append(String.format(Locale.US, "   Загрузка: %s %.1f%%\n",
                buildProgressBar(metrics.physicalUsagePercent()),
                metrics.physicalUsagePercent()));
        sb.append(String.format("   Использовано: %s\n", formatSize(metrics.usedPhysicalBytes(), unitMode)));
        sb.append(String.format("   Свободно: %s\n", formatSize(metrics.freePhysicalBytes(), unitMode)));
        sb.append(String.format("   Всего: %s\n\n", formatSize(metrics.totalPhysicalBytes(), unitMode)));

        // Блок файла подкачки (Swap)
        sb.append("🔸 Файл подкачки (Swap):\n");
        if (metrics.totalSwapBytes() > 0) {
            sb.append(String.format(Locale.US, "   Загрузка: %s %.1f%%\n",
                    buildProgressBar(metrics.swapUsagePercent()),
                    metrics.swapUsagePercent()));
            sb.append(String.format("   Использовано: %s\n", formatSize(metrics.usedSwapBytes(), unitMode)));
            sb.append(String.format("   Свободно: %s\n", formatSize(metrics.freeSwapBytes(), unitMode)));
            sb.append(String.format("   Всего: %s", formatSize(metrics.totalSwapBytes(), unitMode)));
        } else {
            sb.append("   Файл подкачки отключён или недоступен (0 Б)");
        }

        return sb.toString();
    }

    private String formatSize(long bytes, String unitMode) {
        if ("mb".equals(unitMode)) {
            return String.format(Locale.US, "%.2f МБ", bytes / BYTES_IN_MB);
        }
        if ("gb".equals(unitMode)) {
            return String.format(Locale.US, "%.2f ГБ", bytes / BYTES_IN_GB);
        }
        if (bytes >= BYTES_IN_GB) {
            return String.format(Locale.US, "%.2f ГБ", bytes / BYTES_IN_GB);
        } else {
            return String.format(Locale.US, "%.2f МБ", bytes / BYTES_IN_MB);
        }
    }

    private String buildProgressBar(double percent) {
        int filled = (int) Math.round((Math.min(100.0, Math.max(0.0, percent)) / 100.0) * PROGRESS_BAR_LENGTH);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < PROGRESS_BAR_LENGTH; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("]");
        return bar.toString();
    }
}
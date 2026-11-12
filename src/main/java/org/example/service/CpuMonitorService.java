package org.example.service;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class CpuMonitorService {

    private static final Path PROC_UPTIME_PATH = Path.of("/proc/uptime");

    private final OperatingSystemMXBean osBean;
    private final RuntimeMXBean runtimeBean;

    public CpuMonitorService() {
        this(ManagementFactory.getOperatingSystemMXBean(), ManagementFactory.getRuntimeMXBean());
    }

    //Конструктор для тестов и инъекции зависимостей
    public CpuMonitorService(OperatingSystemMXBean osBean, RuntimeMXBean runtimeBean) {
        this.osBean = osBean;
        this.runtimeBean = runtimeBean;
    }

    /**
     *Возвращает общую загрузку CPU в процентах
     */
    public double getSystemCpuLoadPercent() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            return normalizeLoadPercent(sunOsBean.getCpuLoad());
        }
        return -1.0;
    }

    /**
     *Возвращает загрузку CPU текущим процессом JVM в процентах
     */
    public double getProcessCpuLoadPercent() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            return normalizeLoadPercent(sunOsBean.getProcessCpuLoad());
        }
        return -1.0;
    }

    /**
     *Возвращает Load Average за 1 минуту или -1.0 (например, на Windows)
     */
    public double getSystemLoadAverage() {
        double loadAvg = osBean.getSystemLoadAverage();
        return (Double.isNaN(loadAvg) || loadAvg < 0.0) ? -1.0 : loadAvg;
    }

    /**
     *Количество доступных логических ядер процессора
     */
    public int getAvailableProcessors() {
        return osBean.getAvailableProcessors();
    }

    /**
     *Аптайм процесса JVM в миллисекундах
     */
    public long getJvmUptimeMillis() {
        return Math.max(0L, runtimeBean.getUptime());
    }

    /**
     *Аптайм ОС в миллисекундах (из /proc/uptime на Linux)
     *Если файл недоступен (например, на Windows), возвращает -1L
     */
    public long getOsUptimeMillis() {
        return readLinuxProcUptimeMillis(PROC_UPTIME_PATH);
    }

    /**
     *Читает аптайм из файла формата /proc/uptime (первое число — секунды с плавающей точкой)
     */
    long readLinuxProcUptimeMillis(Path procUptimePath) {
        if (procUptimePath == null || !Files.isReadable(procUptimePath)) {
            return -1L;
        }
        try {
            String content = Files.readString(procUptimePath).trim();
            if (content.isEmpty()) {
                return -1L;
            }
            String[] parts = content.split("\\s+");
            double seconds = Double.parseDouble(parts[0]);
            if (Double.isNaN(seconds) || seconds < 0.0) {
                return -1L;
            }
            return (long) (seconds * 1000L);
        } catch (IOException | NumberFormatException e) {
            return -1L;
        }
    }

    /**
     *Переводит сырое значение в проценты
     *Если значение отрицательное или NaN, возвращает -1.0
     */
    public double normalizeLoadPercent(double rawLoad) {
        if (Double.isNaN(rawLoad) || rawLoad < 0.0) {
            return -1.0;
        }
        double percent = rawLoad * 100.0;
        return Math.min(100.0, percent);
    }

    /**
     *Форматирует миллисекунды в строку вида: "X дн. Y ч. Z мин. W сек."
     */
    public String formatDuration(long millis) {
        if (millis < 0L) {
            return "Н/Д";
        }
        long totalSeconds = millis / 1000L;
        long days = totalSeconds / 86400L;
        long hours = (totalSeconds % 86400L) / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" дн. ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append(" ч. ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append(" мин. ");
        }
        sb.append(seconds).append(" сек.");

        return sb.toString();
    }

    /**
     *Формирует текстовый отчет для команды /cpu (и для дашборда /status в задаче 2.4)
     */
    public String getCpuStatusReport() {
        double sysCpu = getSystemCpuLoadPercent();
        double procCpu = getProcessCpuLoadPercent();
        double loadAvg = getSystemLoadAverage();
        int cores = getAvailableProcessors();

        String sysCpuStr = sysCpu >= 0 ? String.format(Locale.US, "%.1f%%", sysCpu) : "Н/Д";
        String procCpuStr = procCpu >= 0 ? String.format(Locale.US, "%.1f%%", procCpu) : "Н/Д";
        String loadAvgStr = loadAvg >= 0 ? String.format(Locale.US, "%.2f", loadAvg) : "Н/Д (Windows)";

        return String.format(
                "💻 Процессор (CPU):\n" +
                        "• Доступно потоков: %d\n" +
                        "• Общая загрузка: %s\n" +
                        "• Загрузка процессом бота: %s\n" +
                        "• Load Average (1 мин): %s",
                cores, sysCpuStr, procCpuStr, loadAvgStr
        );
    }

    /**
     *Формирует текстовый отчет для команды /uptime
     */
    public String getUptimeReport() {
        long jvmUptime = getJvmUptimeMillis();
        long osUptime = getOsUptimeMillis();

        StringBuilder sb = new StringBuilder("⏱ Время непрерывной работы:\n");
        sb.append("• Аптайм бота (JVM): ").append(formatDuration(jvmUptime));

        if (osUptime >= 0L) {
            sb.append("\n• Аптайм сервера (ОС): ").append(formatDuration(osUptime));
        }
        return sb.toString();
    }
}
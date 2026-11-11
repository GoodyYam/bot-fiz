package org.example.service;

import com.sun.management.OperatingSystemMXBean;
import org.example.service.model.RamMetrics;

import java.lang.management.ManagementFactory;

public class DefaultSystemMetricsService implements SystemMetricsService {

    private final OperatingSystemMXBean osBean;

    public DefaultSystemMetricsService() {
        var platformBean = ManagementFactory.getOperatingSystemMXBean();
        if (platformBean instanceof OperatingSystemMXBean sunOsBean) {
            this.osBean = sunOsBean;
        } else {
            throw new IllegalStateException("Платформа не поддерживает расширенный OperatingSystemMXBean");
        }
    }

    @Override
    public RamMetrics getRamMetrics() {
        long totalPhysical = osBean.getTotalMemorySize();
        long freePhysical = osBean.getFreeMemorySize();
        long totalSwap = osBean.getTotalSwapSpaceSize();
        long freeSwap = osBean.getFreeSwapSpaceSize();

        return new RamMetrics(totalPhysical, freePhysical, totalSwap, freeSwap);
    }

    @Override
    public double getCpuLoadPercent() {
        double load = osBean.getCpuLoad();
        if (load < 0) {
            return 0.0;
        }
        return load * 100.0;
    }

    @Override
    public int getAvailableProcessors() {
        return osBean.getAvailableProcessors();
    }
}
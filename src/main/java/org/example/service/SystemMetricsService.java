package org.example.service;

import org.example.service.model.RamMetrics;

public interface SystemMetricsService {
    RamMetrics getRamMetrics();
    double getCpuLoadPercent();
    int getAvailableProcessors();
}
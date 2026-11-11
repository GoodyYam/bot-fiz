package org.example.service.model;

public record RamMetrics(
        long totalPhysicalBytes,
        long freePhysicalBytes,
        long totalSwapBytes,
        long freeSwapBytes
) {
    public long usedPhysicalBytes() {
        return Math.max(0, totalPhysicalBytes - freePhysicalBytes);
    }

    public double physicalUsagePercent() {
        if (totalPhysicalBytes <= 0) {
            return 0.0;
        }
        return (usedPhysicalBytes() * 100.0) / totalPhysicalBytes;
    }

    public long usedSwapBytes() {
        return Math.max(0, totalSwapBytes - freeSwapBytes);
    }

    public double swapUsagePercent() {
        if (totalSwapBytes <= 0) {
            return 0.0;
        }
        return (usedSwapBytes() * 100.0) / totalSwapBytes;
    }
}
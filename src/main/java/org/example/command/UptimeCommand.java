package org.example.command;

import org.example.service.CpuMonitorService;

public class UptimeCommand implements Command {

    private final CpuMonitorService cpuMonitorService;

    public UptimeCommand() {
        this(new CpuMonitorService());
    }

    public UptimeCommand(CpuMonitorService cpuMonitorService) {
        this.cpuMonitorService = cpuMonitorService;
    }

    @Override
    public String getName() {
        return "uptime";
    }

    @Override
    public String getDescription() {
        return "Показать время непрерывной работы бота и сервера";
    }

    @Override
    public String execute(String[] args) {
        return cpuMonitorService.getUptimeReport();
    }
}
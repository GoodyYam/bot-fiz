package org.example.command;

import org.example.service.CpuMonitorService;

public class CpuCommand implements Command {

    private final CpuMonitorService cpuMonitorService;

    public CpuCommand() {
        this(new CpuMonitorService());
    }

    public CpuCommand(CpuMonitorService cpuMonitorService) {
        this.cpuMonitorService = cpuMonitorService;
    }

    @Override
    public String getName() {
        return "cpu";
    }

    @Override
    public String getDescription() {
        return "Показать текущую нагрузку на процессор и количество ядер";
    }

    @Override
    public String execute(String[] args) {
        return cpuMonitorService.getCpuStatusReport();
    }
}
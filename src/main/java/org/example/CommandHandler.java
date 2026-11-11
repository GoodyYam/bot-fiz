package org.example;

import org.example.command.AboutCommand;
import org.example.command.AuthorCommand;
import org.example.command.Command;
import org.example.command.HelpCommand;
import org.example.command.*;
import org.example.command.CpuCommand;
import org.example.command.UptimeCommand;
import org.example.command.RamCommand;
import org.example.service.DefaultSystemMetricsService;
import org.example.service.SystemMetricsService;
import org.example.command.StatusCommand;
import org.example.service.CpuMonitorService;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandHandler {
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public CommandHandler() {
        this(new DefaultSystemMetricsService());
    }

    public CommandHandler(SystemMetricsService metricsService) {
        registerCommand(new AuthorCommand());
        registerCommand(new AboutCommand());
        registerCommand(new RamCommand(metricsService));
        registerCommand(new CpuCommand());
        registerCommand(new UptimeCommand());
        registerCommand(new StatusCommand(metricsService, new CpuMonitorService()));

        registerCommand(new HelpCommand(commands));
    }

    public void registerCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public String handle(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return "Пустой ввод.";
        }

        String[] parts = rawInput.trim().split("\\s+");
        String commandName = parts[0].replace("/", "");
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        Command command = commands.get(commandName);
        if (command == null) {
            return "Неизвестная команда. Введите /help для списка команд.";
        }

        return command.execute(args);
    }
}
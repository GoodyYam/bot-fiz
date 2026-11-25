package org.example;

import org.example.command.AboutCommand;
import org.example.command.AuthorCommand;
import org.example.command.Command;
import org.example.command.HelpCommand;
import org.example.command.*;
import org.example.command.CpuCommand;
import org.example.command.UptimeCommand;
import org.example.command.RamCommand;
import org.example.command.ServiceCommand;
import org.example.service.DefaultSystemMetricsService;
import org.example.service.SystemMetricsService;
import org.example.command.StatusCommand;
import org.example.service.CpuMonitorService;
import org.example.command.CheckCommand;
import org.example.service.AdminAuthService;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandHandler {
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final AdminAuthService adminAuthService;

    public CommandHandler() {
        this(new DefaultSystemMetricsService(), new AdminAuthService());
    }

    public CommandHandler(SystemMetricsService metricsService, AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
        registerCommand(new AuthorCommand());
        registerCommand(new AboutCommand());
        registerCommand(new RamCommand(metricsService));
        registerCommand(new CpuCommand());
        registerCommand(new UptimeCommand());
        registerCommand(new StatusCommand(metricsService, new CpuMonitorService()));
        registerCommand(new CheckCommand());
        registerCommand(new ServiceCommand());

        registerCommand(new HelpCommand(commands));
    }

    public void registerCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public String handle(String rawInput, long userId) {
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

        if (command.isPrivate() && !adminAuthService.isAdmin(userId)) {
            System.out.println("БЛОКИРОВКА: Юзер " + userId + " попытался вызвать приватную команду /" + commandName);
            return "Отказ в доступе. Эта команда предназначена только для администраторов сервера.";
        }

        return command.execute(args);
    }
}
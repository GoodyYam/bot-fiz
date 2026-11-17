package org.example.command;

import org.example.service.NetworkCheckService;
import org.example.service.model.CheckResult;

public class CheckCommand implements Command {

    private final NetworkCheckService networkCheckService;

    public CheckCommand() {
        this(new NetworkCheckService());
    }

    public CheckCommand(NetworkCheckService networkCheckService) {
        this.networkCheckService = networkCheckService;
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Проверить доступность TCP-порта на хосте (/check <host> <port>)";
    }

    @Override
    public String execute(String[] args) {
        if (args == null || args.length != 2) {
            return "Использование: /check <host> <port>\nПример: /check google.com 443";
        }

        String host = args[0].trim();
        if (!networkCheckService.isValidHost(host)) {
            return "Ошибка: некорректное имя хоста или IP-адрес.";
        }

        int port;
        try {
            port = Integer.parseInt(args[1].trim());
        } catch (NumberFormatException e) {
            return "Ошибка: порт должен быть целым числом от 1 до 65535.";
        }

        if (!networkCheckService.isValidPort(port)) {
            return "Ошибка: порт должен быть в диапазоне от 1 до 65535.";
        }

        CheckResult result = networkCheckService.checkTcpPort(host, port);
        if (result.reachable()) {
            return String.format(
                    "Проверка %s:%d\n• Статус: Доступен ✅\n• Время отклика (RTT): %d мс",
                    host, port, result.rttMillis()
            );
        }

        return String.format(
                "Проверка %s:%d\n• Статус: Недоступен ❌\n• Причина: %s",
                host, port, result.errorMessage()
        );
    }
}
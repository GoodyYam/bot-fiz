package org.example.command;

import org.example.service.ServiceCheckService;
import org.example.service.model.ServiceStatusResult;

import java.util.stream.Collectors;

public class ServiceCommand implements Command {

    private final ServiceCheckService serviceCheckService;

    public ServiceCommand() {
        this(new ServiceCheckService());
    }

    public ServiceCommand(ServiceCheckService serviceCheckService) {
        this.serviceCheckService = serviceCheckService;
    }

    @Override
    public String getName() {
        return "service";
    }

    @Override
    public String getDescription() {
        return "Проверить статус системной службы (/service <name>)";
    }

    @Override
    public String execute(String[] args) {
        String allowedList = serviceCheckService.getAllowedServices().stream()
                .sorted()
                .collect(Collectors.joining(", "));

        if (args == null || args.length == 0) {
            return "Использование: /service <name>\n" +
                    "Разрешенные службы: " + allowedList + "\n" +
                    "Пример: /service nginx";
        }

        String rawServiceName = String.join(" ", args).trim();
        if (rawServiceName.isEmpty()) {
            return "Использование: /service <name>\n" +
                    "Разрешенные службы: " + allowedList + "\n" +
                    "Пример: /service nginx";
        }

        if (!serviceCheckService.isValidServiceName(rawServiceName)) {
            return "Ошибка безопасности: недопустимое имя службы. " +
                    "Обнаружены запрещенные символы (разрешены только a-z, A-Z, 0-9, '_', '-').";
        }

        if (!serviceCheckService.isAllowedService(rawServiceName)) {
            return String.format(
                    "Ошибка: служба '%s' не входит в белый список разрешенных.\nДоступные службы: %s",
                    rawServiceName, allowedList
            );
        }

        String normalizedName = rawServiceName.toLowerCase();
        ServiceStatusResult result = serviceCheckService.checkServiceStatus(normalizedName);

        if (result.active()) {
            return String.format(
                    "Служба %s\n• Статус: Активна (active) ✅",
                    normalizedName
            );
        }

        return String.format(
                "Служба %s\n• Статус: Неактивна (%s) ❌\n• Причина: %s",
                normalizedName, result.status(), result.errorMessage()
        );
    }
}
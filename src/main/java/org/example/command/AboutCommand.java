package org.example.command;

public class AboutCommand implements Command {
    @Override
    public String getName() {
        return "about";
    }

    @Override
    public String getDescription() {
        return "Выводит информацию о назначении бота";
    }

    @Override
    public String execute(String[] args) {
        return "Бот для удаленного мониторинга и администрирования серверов. Предоставляет сводку по утилизации ресурсов (CPU, RAM, диск), проверяет доступность сервисов и отправляет экстренные алерты при сбоях.";
    }
}
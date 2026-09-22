package org.example;

import org.example.command.AboutCommand;
import org.example.command.AuthorCommand;
import org.example.command.Command;
import org.example.command.HelpCommand;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandHandler {
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public CommandHandler() {
        registerCommand(new AuthorCommand());
        registerCommand(new AboutCommand());
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
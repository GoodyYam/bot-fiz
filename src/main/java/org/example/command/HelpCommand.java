package org.example.command;

import java.util.Map;

public class HelpCommand implements Command {
    private final Map<String, Command> commands;

    public HelpCommand(Map<String, Command> commands) {
        this.commands = commands;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Справка по командам: /help или /help <command>";
    }

    @Override
    public String execute(String[] args) {
        if (args.length > 0) {
            String target = args[0].replace("/", "");
            Command cmd = commands.get(target);
            if (cmd != null) {
                return "/" + cmd.getName() + " — " + cmd.getDescription();
            }
            return "Команда /" + target + " не найдена.";
        }

        StringBuilder sb = new StringBuilder("Список доступных команд:\n");
        for (Command cmd : commands.values()) {
            sb.append("/").append(cmd.getName())
                    .append(" — ").append(cmd.getDescription())
                    .append("\n");
        }
        return sb.toString().trim();
    }
}
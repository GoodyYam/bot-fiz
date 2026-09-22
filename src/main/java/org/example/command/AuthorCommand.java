package org.example.command;

public class AuthorCommand implements Command {
    @Override
    public String getName() {
        return "author";
    }

    @Override
    public String getDescription() {
        return "Выводит информацию об авторах проекта";
    }

    @Override
    public String execute(String[] args) {
        return "Авторы проекта: Фрунзе Вадим, Дерда Егор";
    }
}
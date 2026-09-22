package org.example.command;

public interface Command {
    String getName();
    String getDescription();
    String execute(String[] args);
}
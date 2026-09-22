package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        CommandHandler handler = new CommandHandler();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Бот запущен. Введите команду (например: /help, /author, /about):");

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            if ("/exit".equalsIgnoreCase(input.trim())) {
                break;
            }
            System.out.println(handler.handle(input));
        }
    }
}
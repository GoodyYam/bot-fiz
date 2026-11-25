package org.example.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminAuthService {

    private final Set<Long> adminIds;

    public AdminAuthService() {
        this(System.getenv("ADMIN_IDS"));
    }
    public AdminAuthService(String adminIdsEnv) {
        if (adminIdsEnv == null || adminIdsEnv.isBlank()) {
            System.err.println("ВНИМАНИЕ: Переменная ADMIN_IDS не задана. Доступ к приватным командам закрыт для всех.");
            this.adminIds = Collections.emptySet();
        } else {
            try {

                this.adminIds = Arrays.stream(adminIdsEnv.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toUnmodifiableSet());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Ошибка парсинга ADMIN_IDS. ID должны быть числами, разделёнными запятой.", e);
            }
        }
    }
    public boolean isAdmin(long userId) {
        return adminIds.contains(userId);
    }
}
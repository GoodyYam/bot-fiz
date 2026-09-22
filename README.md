# 🖥️ Server Monitoring Telegram Bot (`bot-fiz`)

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-orange.svg?style=flat&logo=openjdk)](https://www.oracle.com/java/)
[![Build Tool](https://img.shields.io/badge/Maven-3.9+-C71A36.svg?style=flat&logo=apachemaven)](https://maven.apache.org/)
[![Telegram API](https://img.shields.io/badge/TelegramBots-7.x-2CA5E0.svg?style=flat&logo=telegram)](https://github.com/rubenlagus/TelegramBots)
[![Testing](https://img.shields.io/badge/JUnit-5.10.2-25A162.svg?style=flat&logo=junit5)](https://junit.org/junit5/)
[![Academic Project](https://img.shields.io/badge/УрФУ-Компьютерная%20безопасность-blue.svg)](https://urfu.ru/)

Модульный Telegram-бот на Java для удалённого мониторинга серверной инфраструктуры, системных ресурсов (CPU, RAM, Disk) и доступности сервисов.

Проект разрабатывается с фокусом на расширяемость, слабую связность компонентов (Decoupling) и безопасность конфиденциальных данных.

---

## 📌 О проекте

При администрировании небольших серверов и пет-проектов разворачивание тяжёлых систем мониторинга (Prometheus + Grafana, Zabbix) часто избыточно по ресурсам.

Данный бот решает задачу **оперативного легковесного мониторинга**: администратор получает статус машины, метрики и алерты о сбоях напрямую в чат Telegram без необходимости постоянного ручного подключения по SSH.

---

## 🏗 Архитектура и принципы проектирования

Архитектура бота разделена на изолированные слои:

```text
[ Telegram API ] 
       │  (Long Polling / JSON updates)
       ▼
[ TelegramBot Adapter ] ── (Сетевой транспорт: OkHttpTelegramClient)
       │  (Передача сырого текста команд)
       ▼
[ CommandHandler ] ─────── (Диспетчер / Маршрутизатор)
       │  (Поиск по словарю зарегистрированных команд)
       ▼
┌───────────────────────────────┐
│     Интерфейс «Command»       │  <── Паттерн «Команда»
└──────────────┬────────────────┘
               ├── AboutCommand
               ├── AuthorCommand
               ├── HelpCommand
               └── [В разработке: Status / Metric Commands]
```

### Ключевые архитектурные решения:
1. **Паттерн «Команда» (Command Pattern):** каждая команда бота (`/author`, `/about`, `/help`) инкапсулирована в отдельный независимый класс, реализующий интерфейс `Command`. Добавление новой функциональности не требует правки логики существующих команд.
2. **Изоляция транспортного уровня:** `CommandHandler` и сами команды ничего не знают о Telegram. Их логика принимает и возвращает обычные строки (`String`), что позволяет тестировать обработку команд через JUnit без поднятия сетевых соединений и моков Telegram API.
3. **Безопасность (Security by Design):** абсолютный запрет на хранение токенов авторизации в кодовой базе. Авторизационные данные передаются исключительно через переменные окружения ОС.

---

## 📂 Структура проекта

```text
bot-fiz/
├── src/
│   ├── main/
│   │   └── java/org/example/
│   │       ├── command/                  # Пакет реализаций команд
│   │       │   ├── Command.java          # Базовый интерфейс контракта
│   │       │   ├── AboutCommand.java     # Описание концепта бота
│   │       │   ├── AuthorCommand.java    # Авторы проекта
│   │       │   └── HelpCommand.java      # Интерактивная справка по системе
│   │       ├── CommandHandler.java       # Диспетчер и маршрутизация ввода
│   │       ├── TelegramBot.java          # Сетевой адаптер (Long Polling)
│   │       └── Main.java                 # Точка входа и инициализация зависимостей
│   └── test/
│       └── java/org/example/
│           └── CommandHandlerTest.java   # Юнит-тесты ядра диспетчера команд
├── pom.xml                               # Конфигурация сборщика Maven
└── README.md
```

---

## ⚡ Доступные команды

| Команда | Описание |
| :--- | :--- |
| `/help` | Показать список всех доступных команд системы |
| `/help <command>` | Получить подробную справку по конкретной команде |
| `/about` | Краткое описание назначения и концепции бота |
| `/author` | Информация о разработчиках проекта |

---

## 🚀 Быстрый старт

### Требования
* **JDK:** версия 17 или 21.
* **Maven:** 3.8+ (или используйте встроенный `mvnw`).
* **Токен бота Telegram:** получается у [@BotFather](https://t.me/BotFather).

### 1. Клонирование репозитория
```bash
git clone https://github.com/your-username/bot-fiz.git
cd bot-fiz
```

### 2. Настройка переменных окружения
Бот считывает токен из переменной `BOT_TOKEN`. Задайте её перед запуском:

* **Linux / macOS:**
  ```bash
  export BOT_TOKEN="123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ"
  ```
* **Windows (cmd):**
  ```cmd
  set BOT_TOKEN=123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ
  ```
* **Windows (PowerShell):**
  ```powershell
  $env:BOT_TOKEN="123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ"
  ```

*(В IntelliJ IDEA переменную можно задать через: **Run** → **Edit Configurations...** → **Environment variables**).*

### 3. Сборка и запуск тестов
```bash
mvn clean test
```

### 4. Запуск приложения
```bash
mvn compile exec:java -Dexec.mainClass="org.example.Main"
```

---

## 🌐 Работа в условиях сетевых ограничений (Proxy / VPN)

В случае проблем с доступом к `api.telegram.org` (например, таймауты подключения в РФ), приложение можно запустить через локальный прокси, передав VM Options JVM:

* **Через SOCKS5-прокси (например, порт 20808):**
  ```bash
  java -DsocksProxyHost=127.0.0.1 -DsocksProxyPort=20808 -jar target/bot-fiz-1.0-SNAPSHOT.jar
  ```
* **Через HTTP/HTTPS-прокси (например, порт 20809):**
  ```bash
  java -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=20809 -jar target/bot-fiz-1.0-SNAPSHOT.jar
  ```

Либо включите режим **TUN (виртуальный адаптер)** в вашем клиенте проксирования (NekoBox, v2rayN, Clash).

---

## 🗺️ Roadmap (Планы развития)

- [x] Базовый каркас командного интерфейса (`Command`, `CommandHandler`).
- [x] Интеграция с Telegram API через Long Polling.
- [x] Покрытие диспетчера юнит-тестами на JUnit 5.
- [ ] **Мониторинг ресурсов хоста:**
    - [ ] Реализация `/cpu` (загрузка процессора, средняя нагрузка Load Average).
    - [ ] Реализация `/ram` (использование оперативной памяти и swap).
    - [ ] Реализация `/disk` (свободное место на смонтированных накопителях).
- [ ] **Сетевой мониторинг:**
    - [ ] Проверка доступности портов и внешних сервисов (ping/healthcheck).
- [ ] **Фоновые алерты:**
    - [ ] Асинхронные уведомления в чат при превышении порога утилизации (RAM > 90%, диск > 95%).

---

## 👥 Авторы

Студенты направления **«Компьютерная безопасность»**  
Институт радиоэлектроники и информационных технологий, **УрФУ**:

* **Вадим**
* **Егор**
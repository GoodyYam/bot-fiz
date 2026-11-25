# 🖥️ Server Monitoring Telegram Bot (`bot-fiz`)

Модульный Telegram-бот на Java для оперативного удалённого мониторинга серверной инфраструктуры, контроля системных ресурсов (CPU, RAM, Disk), проверки доступности сетевых сервисов и системных служб ОС.

Проект спроектирован с упором на чистую архитектуру, слабую связность компонентов (*Loose Coupling*), расширяемость функционала и соблюдение практик безопасной разработки (*Security by Design*).

---

## 📑 Содержание

1. [О проекте](#-о-проекте)
2. [Технологический стек](#-технологический-стек)
3. [Архитектура и принципы проектирования](#-архитектура-и-принципы-проектирования)
4. [Структура проекта](#-структура-проекта)
5. [Доступные команды](#-доступные-команды)
6. [Декомпозиция проекта](#-декомпозиция-проекта)
7. [Быстрый старт](#-быстрый-старт)
8. [Тестирование](#-тестирование)

---

## 📌 О проекте

При администрировании небольших серверов, VPS, лабораторных стендов и пет-проектов развёртывание тяжеловесных систем мониторинга (связки *Prometheus + Grafana*, *Zabbix*, *ELK-стек*) зачастую избыточно: они требуют выделенных вычислительных ресурсов, сложной настройки и постоянного обслуживания.

**`bot-fiz`** решает задачу **легковесного и оперативного мониторинга**:
* **Мгновенный доступ:** администратор запрашивает статус сервера и метрики напрямую через интерфейс Telegram без необходимости ручного подключения по SSH.
* **Минимальное потребление ресурсов:** приложение работает в рамках одного легковесного JVM-процесса или изолированного Docker-контейнера.
* **Безопасный доступ:** приватные метрики и проверки закрыты авторизацией по списку `ADMIN_IDS`.
* **Простая расширяемость:** модульная структура позволяет добавлять новые метрики и проверки за считанные минуты.
* **Проактивный контроль:** архитектура закладывает возможность отправки автоматических уведомлений (алертов) при критической нагрузке на узлы.

---

## 🛠 Технологический стек

| Компонент | Технология / Библиотека | Версия | Назначение в проекте |
| :--- | :--- | :---: | :--- |
| **Язык программирования** | Java (OpenJDK) | `17` | Основной язык разработки, использование современного API (`record`, `nio`) |
| **Система сборки** | Apache Maven + `maven-shade-plugin` | `3.8+` / `3.6.0` | Управление зависимостями, тестирование и сборка автономного `fat-JAR` |
| **Telegram API** | `telegrambots-longpolling` | `10.3.0` | Получение обновлений от серверов Telegram по технологии Long Polling |
| **HTTP-клиент** | `telegrambots-client` | `10.3.0` | Сетевой транспорт для отправки ответов в Telegram API |
| **Логирование** | `slf4j-simple` | `2.0.9` | Базовое логирование событий приложения |
| **Системные и сетевые проверки** | `java.lang.management`, `java.nio.file`, `java.net.Socket`, `ProcessBuilder` | `JDK` | Сбор метрик CPU, RAM, Swap, Uptime, Disk, проверка TCP-портов и демонов ОС |
| **Контейнеризация** | Docker / Docker Compose | `Multi-stage` | Изолированная сборка и развёртывание на сервере в одну команду |
| **Тестирование** | JUnit Jupiter (JUnit 5) | `5.10.2` | Модульное (unit) тестирование ядра, авторизации, сервисов и команд |

---

## 🏗 Архитектура и принципы проектирования

Архитектура приложения разделена на независимые функциональные слои, что исключает жёсткую привязку бизнес-логики к внешнему API мессенджера:

```text
[ Telegram Bot API ]
         │
         │ (Long Polling / JSON Updates)
         ▼
[ TelegramBot Adapter ] ─── (Сетевой транспорт + извлечение userId и текста)
         │
         ▼
[ CommandHandler ] ──────── (Диспетчер, парсинг аргументов + AdminAuthService)
         │
         ▼
┌─────────────────────────────────────────┐
│           Интерфейс «Command»           │  <── Паттерн «Команда» (Command Pattern)
└────────────────────┬────────────────────┘
                     ├── Публичные: AboutCommand, AuthorCommand, HelpCommand
                     ├── Приватные (Host Metrics):
                     │      ├── RamCommand       (/ram)      ──► [SystemMetricsService]
                     │      ├── CpuCommand       (/cpu)      ──► [CpuMonitorService]
                     │      ├── UptimeCommand    (/uptime)   ──► [CpuMonitorService]
                     │      └── StatusCommand    (/status)   ──► [CPU, RAM, Disk, Uptime]
                     └── Приватные (Network & System):
                            ├── CheckCommand     (/check)    ──► [NetworkCheckService]
                            └── ServiceCommand   (/service)  ──► [ServiceCheckService]
```

### Ключевые архитектурные решения

1. **Паттерн «Команда» (Command Pattern):**
   Каждая команда бота инкапсулирована в отдельный класс, реализующий единый контракт `Command`. Это обеспечивает соблюдение принципа открытости/закрытости (*Open-Closed Principle, SOLID*): для добавления новой команды достаточно создать новый класс и зарегистрировать его в диспетчере.
2. **Изоляция транспортного уровня (Decoupling):**
   Классы бизнес-логики (`CommandHandler`, сервисы и реализации `Command`) не зависят от библиотеки `TelegramBots`. Сетевой слой (`TelegramBot`) выступает исключительно в роли адаптера.
3. **Высокая тестируемость (Testability):**
   Благодаря инъекции зависимостей через конструкторы, тестирование маршрутизации, прав доступа, расчёта метрик и сетевых проверок выполняется чистыми JUnit-тестами со стабами без обращения к Telegram API.
4. **Безопасность по умолчанию (Security by Design):**
   Секреты не хранятся в коде: `BOT_TOKEN` и `ADMIN_IDS` передаются через переменные окружения (или локальный `.env`, исключённый в `.gitignore`). При проверке служб ОС применяется строгая валидация и белый список против *Command Injection*.

---

## 📂 Структура проекта

```text
bot-fiz/
├── src/
│   ├── main/
│   │   └── java/org/example/
│   │       ├── command/                             # Пакет реализаций команд (Command Pattern)
│   │       │   ├── Command.java                     # Базовый интерфейс контракта команды
│   │       │   ├── AboutCommand.java                # Команда /about — назначение и концепция бота
│   │       │   ├── AuthorCommand.java               # Команда /author — сведения о разработчиках
│   │       │   ├── HelpCommand.java                 # Команда /help — динамическая справочная система
│   │       │   ├── RamCommand.java                  # Команда /ram — мониторинг ОЗУ и Swap
│   │       │   ├── CpuCommand.java                  # Команда /cpu — загрузка процессора и Load Average
│   │       │   ├── UptimeCommand.java               # Команда /uptime — время непрерывной работы JVM и ОС
│   │       │   ├── StatusCommand.java               # Команда /status — сводный дашборд состояния сервера
│   │       │   ├── CheckCommand.java                # Команда /check — проверка доступности TCP-порта
│   │       │   └── ServiceCommand.java              # Команда /service — статус системных служб ОС
│   │       ├── service/                             # Сервисный слой мониторинга и безопасности
│   │       │   ├── model/
│   │       │   │   ├── RamMetrics.java              # Record-модель показателей физической памяти и Swap
│   │       │   │   ├── CheckResult.java             # Record-модель результата проверки TCP-порта
│   │       │   │   └── ServiceStatusResult.java     # Record-модель статуса системной службы
│   │       │   ├── AdminAuthService.java            # Проверка прав администратора по ADMIN_IDS
│   │       │   ├── SystemMetricsService.java        # Интерфейс сервиса получения системных метрик
│   │       │   ├── DefaultSystemMetricsService.java # Реализация сбора метрик через OperatingSystemMXBean
│   │       │   ├── CpuMonitorService.java           # Сервис мониторинга CPU, Load Average и /proc/uptime
│   │       │   ├── NetworkCheckService.java         # Проверка доступности хостов и портов через Socket
│   │       │   └── ServiceCheckService.java         # Безопасный опрос служб ОС через ProcessBuilder
│   │       ├── CommandHandler.java                  # Диспетчер: авторизация, парсинг и маршрутизация
│   │       ├── TelegramBot.java                     # Адаптер Telegram API (Long Polling)
│   │       └── Main.java                            # Точка входа, валидация окружения и запуск бота
│   └── test/
│       └── java/org/example/
│           ├── command/
│           │   ├── CheckCommandTest.java            # Тесты команды /check
│           │   ├── CpuAndUptimeCommandsTest.java    # Тесты команд /cpu и /uptime
│           │   ├── RamCommandTest.java              # Тесты команды /ram
│           │   ├── ServiceCommandTest.java          # Тесты команды /service
│           │   └── StatusCommandTest.java           # Тесты сводного дашборда /status
│           ├── service/
│           │   ├── AdminAuthServiceTest.java        # Тесты white-листа администраторов
│           │   ├── CpuMonitorServiceTest.java       # Тесты CPU, парсера /proc/uptime и времени
│           │   ├── NetworkCheckServiceTest.java     # Тесты валидации хостов/портов и локального Socket
│           │   └── ServiceCheckServiceTest.java     # Тесты белого списка и защиты от Command Injection
│           └── CommandHandlerTest.java              # Тесты диспетчера и контроля доступа
├── Dockerfile                                       # Многоступенчатая сборка Docker-образа (Maven → JRE Alpine)
├── docker-compose.yml                               # Конфигурация запуска контейнера с пробросом env
├── .dockerignore                                    # Исключения для контекста сборки Docker
├── .env.example                                     # Шаблон переменных окружения
├── pom.xml                                          # Зависимости и конфигурация maven-shade-plugin
└── README.md                                        # Документация проекта
```

---

## ⚡ Доступные команды

| Команда | Аргументы | Доступ | Описание |
| :--- | :---: | :---: | :--- |
| `/help` | `[имя_команды]` | Публичный | Список всех команд или детальная справка по указанной команде |
| `/about` | — | Публичный | Информация о назначении, возможностях и концепции бота |
| `/author` | — | Публичный | Сведения о разработчиках проекта |
| `/ram` | — | Админ | Статистика использования физической памяти (ОЗУ) и раздела подкачки (Swap) |
| `/cpu` | — | Админ | Количество потоков, общая загрузка CPU, нагрузка процесса бота и Load Average |
| `/uptime` | — | Админ | Время непрерывной работы процесса бота (JVM) и операционной системы сервера |
| `/status` | — | Админ | Сводный дашборд состояния сервера (CPU, RAM, Swap, Disk, Uptime) с прогресс-барами |
| `/check` | `<host> <port>` | Админ | Проверка доступности TCP-порта на хосте и замер времени отклика (RTT) |
| `/service` | `<name>` | Админ | Безопасная проверка статуса системной службы из белого списка |

---

## 🧩 Декомпозиция проекта

Разработка ведется итеративно (по задачам/спринтам). Каждая новая задача изолирована в отдельной Git-ветке, расширяет функционал бота без поломки обратной совместимости и покрывается модульными тестами (JUnit 5).

### Этап 1. Базовый каркас и транспортный слой (Выполнено)
- [x] **Задача 1.1. Архитектурное ядро и CLI-режим** — `Command`, `CommandHandler`, `/about`, `/author`, `/help`, `CommandHandlerTest`.
- [x] **Задача 1.2. Интеграция с Telegram Bots API (Long Polling)** — `TelegramBot`, чтение `BOT_TOKEN` из переменных окружения.

### Этап 2. Локальный мониторинг ресурсов сервера (Выполнено)
- [x] **Задача 2.1. Мониторинг оперативной памяти (`/ram`)** — `SystemMetricsService`, `RamMetrics`, `RamCommand`, `RamCommandTest`.
- [x] **Задача 2.2. Мониторинг нагрузки процессора и аптайма (`/cpu`, `/uptime`)** — `CpuMonitorService`, `CpuCommand`, `UptimeCommand` и тесты.
- [x] **Задача 2.3. Мониторинг дискового пространства** — анализ файловых хранилищ на базе `java.nio.file.FileStore`.
- [x] **Задача 2.4. Сводный дашборд состояния (`/status`)** — `StatusCommand` с прогресс-барами и `StatusCommandTest`.

### Этап 3. Безопасность, контроль доступа и Деплой (Выполнено)
- [x] **Задача 3.1. Авторизация по White-листу (`Admin Filter`)** — `AdminAuthService`, проверка `userId` по переменной `ADMIN_IDS`, разделение на публичные и приватные команды, `AdminAuthServiceTest`.
- [x] **Задача 3.2. Сборка fat-JAR и Docker-образ** — настройка `maven-shade-plugin` в `pom.xml`, многоступенчатый `Dockerfile` (`maven:3.9.9-eclipse-temurin-17` → `eclipse-temurin:17-jre-alpine`), `.dockerignore`, `docker-compose.yml` и `.env.example`.

### Этап 4. Автоматический мониторинг и система уведомлений (Alerting)
- [ ] **Задача 4.1. Фоновый планировщик пороговых значений (`AlertScheduler`)**
  - **Что нового:** Фоновый поток на базе `ScheduledExecutorService` и команда настройки/просмотра порогов `/alerts`.
  - **Функционал:** Опрос CPU, RAM и диска каждые N секунд. При превышении порога бот сам отправляет уведомление всем `ADMIN_IDS` с учётом механизма *cooldown*.
  - **Тесты:** Тестирование срабатывания триггеров и подавления повторных алертов.

### Этап 5. Мониторинг процессов, служб и сети (Выполнено)
- [x] **Задача 5.1. Сетевой чекер доступности портов и хостов (`/check <host> <port>`)** — `NetworkCheckService`, `CheckResult`, `CheckCommand` и unit-тесты.
- [x] **Задача 5.2. Безопасный контроль системных служб (`/service <name>`)** — `ServiceCheckService`, `ServiceStatusResult`, `ServiceCommand` с белым списком и защитой от *Command Injection*.

---

## 🚀 Быстрый старт

### Вариант 1. Запуск через Docker Compose (рекомендуется для сервера)

Требуется установленный **Docker** и **Docker Compose**.

1. Клонируйте репозиторий и создайте файл `.env` на основе шаблона:
   ```bash
   git clone [https://github.com/GoodyYam/bot-fiz.git](https://github.com/GoodyYam/bot-fiz.git)
   cd bot-fiz
   cp .env.example .env
   ```
2. Укажите в `.env` свой токен от [@BotFather](https://t.me/BotFather) и Telegram ID администраторов через запятую:
   ```env
   BOT_TOKEN=123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ
   ADMIN_IDS=123456789,987654321
   ```
3. Соберите и запустите контейнер в фоне:
   ```bash
   docker compose up -d --build
   ```
   Просмотр логов и остановка:
   ```bash
   docker compose logs -f
   docker compose down
   ```

### Вариант 2. Сборка и запуск автономного `fat-JAR` (JDK 17+)

1. Соберите проект (прогонятся тесты и создастся `target/bot-fiz.jar`):
   ```bash
   mvn clean package
   ```
   *(В IntelliJ IDEA можно запустить фазу **Lifecycle → package** на панели Maven).*

2. Задайте переменные окружения и запустите JAR:
  * **Linux / macOS:**
    ```bash
    export BOT_TOKEN="ваш_токен"
    export ADMIN_IDS="ваш_telegram_id"
    java -jar target/bot-fiz.jar
    ```
  * **Windows (PowerShell):**
    ```powershell
    $env:BOT_TOKEN="ваш_токен"
    $env:ADMIN_IDS="ваш_telegram_id"
    java -jar target/bot-fiz.jar
    ```

---

## 🧪 Тестирование

В проекте используется фреймворк **JUnit 5 (Jupiter)**. Модульные тесты расположены в `src/test/java/org/example/` и покрывают:
* **`CommandHandlerTest` и `AdminAuthServiceTest`**: маршрутизацию команд, работу `/help <command>`, парсинг `ADMIN_IDS` и блокировку приватных команд для неавторизованных пользователей;
* **`RamCommandTest`, `CpuMonitorServiceTest`, `CpuAndUptimeCommandsTest`, `StatusCommandTest`**: расчёт ОЗУ/Swap, нормализацию загрузки CPU, парсинг `/proc/uptime`, прогресс-бары и форматирование отчётов;
* **`NetworkCheckServiceTest` и `CheckCommandTest`**: валидацию хостов/портов (`1–65535`) и проверку подключения к локальному `ServerSocket`;
* **`ServiceCheckServiceTest` и `ServiceCommandTest`**: проверку белого списка служб и блокировку попыток *Command Injection*.

Запуск всех тестов:
```bash
mvn test
```
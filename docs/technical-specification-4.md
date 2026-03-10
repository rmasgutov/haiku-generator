# Техническая спецификация: Логирование и метрики ChatClient (Spring AI built-in)

## Контекст

Приложение генерирует хайку через `ChatClient` (Spring AI 2.0.0-M2 + Anthropic). Сейчас:
- Нет логирования вызовов к LLM
- Нет метрик по задержке, токенам, ошибкам
- `spring-boot-starter-actuator` не подключён

Spring AI содержит встроенную поддержку Micrometer Observation и `SimpleLoggerAdvisor`.
Цель спецификации — включить эти механизмы с минимальными изменениями кода.

---

## Что даёт Spring AI "из коробки"

### Метрики (автоматически при наличии Actuator)

**ChatClient (`gen_ai.chat.client.operation`)** — таймер на каждый `call()`/`stream()`:

| Prometheus метрика | Описание |
|---|---|
| `gen_ai_chat_client_operation_seconds_count` | Кол-во завершённых вызовов |
| `gen_ai_chat_client_operation_seconds_sum` | Суммарное время |
| `gen_ai_chat_client_operation_seconds_max` | Максимальное время |
| `gen_ai_chat_client_operation_active_count` | Вызовов в процессе выполнения |

Теги (low cardinality): `gen_ai.operation.name=framework`, `gen_ai.system=spring_ai`, `spring.ai.chat.client.stream=false`, `spring.ai.kind=chat_client`

**ChatModel — Anthropic (`gen_ai.client.operation`)** — таймер на HTTP-вызов к API:

Теги: `gen_ai.request.model=claude-sonnet-4-6`, `gen_ai.system=anthropic`, `gen_ai.operation.name=chat`

### Логирование (SimpleLoggerAdvisor)

`SimpleLoggerAdvisor` — встроенный Advisor Spring AI. При уровне `DEBUG` логирует:
- запрос (промпт, параметры)
- ответ (содержимое, metadata)

Логгер: `org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor`

### Логирование содержимого через Observation

Свойства (отключены по умолчанию из соображений приватности):
- `spring.ai.chat.client.observations.log-prompt=true` — логировать содержимое промпта
- `spring.ai.chat.client.observations.log-completion=true` — логировать ответ модели
- `spring.ai.chat.observations.log-prompt=true` — то же для ChatModel уровня
- `spring.ai.chat.observations.log-completion=true`

---

## Изменения

### 1. `build.gradle.kts` — добавить зависимости

```kotlin
implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("io.micrometer:micrometer-registry-prometheus")
```

Версии управляются Spring Boot BOM — явно не указывать. Actuator активирует всю автоконфигурацию Micrometer Observation в Spring AI.

### 2. `HaikuService.kt` — добавить `SimpleLoggerAdvisor`

Единственное изменение кода: добавить `SimpleLoggerAdvisor` в цепочку advisors.

```kotlin
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor

private val chatClient: ChatClient = chatClientBuilder
    .defaultAdvisors(
        MessageChatMemoryAdvisor.builder(chatMemory).build(),
        SimpleLoggerAdvisor()   // ← добавить
    )
    .build()
```

`SimpleLoggerAdvisor` должен быть последним в цепочке (ближе к модели), чтобы логировать финальный запрос после всех преобразований advisors.

**Опционально** — кастомизация формата (если нужно скрыть содержимое промптов, логировать только метаданные):

```kotlin
SimpleLoggerAdvisor(
    requestToString = { req -> "model=${req.chatOptions()?.model} advisors=${req.advisors().size}" },
    responseToString = { res -> "tokens=${res.metadata?.usage?.totalTokens} results=${res.results?.size}" },
    order = Int.MAX_VALUE
)
```

### 3. `application.properties` — конфигурация

```properties
# --- Actuator ---
management.endpoints.web.exposure.include=health,prometheus,metrics
management.endpoint.prometheus.access=unrestricted

# --- Spring AI Observability ---
# Уровень DEBUG для SimpleLoggerAdvisor
logging.level.org.springframework.ai.chat.client.advisor=DEBUG

# Логирование содержимого (отключить в prod, включать только для отладки)
spring.ai.chat.client.observations.log-prompt=false
spring.ai.chat.client.observations.log-completion=false
spring.ai.chat.observations.log-prompt=false
spring.ai.chat.observations.log-completion=false
```

---

## Итоговые метрики после внедрения

| Метрика | Источник |
|---|---|
| `gen_ai_chat_client_operation_seconds_*` | Spring AI авто-конфигурация |
| `gen_ai_client_operation_seconds_*` | Spring AI авто-конфигурация (Anthropic уровень) |
| `http_server_requests_seconds_*` | Spring Boot авто-конфигурация |

Доступны через `GET /actuator/prometheus`.

---

## Итоговые файлы для изменения

| Файл | Изменение |
|---|---|
| `build.gradle.kts` | +2 зависимости (`actuator`, `micrometer-registry-prometheus`) |
| `HaikuService.kt` | Добавить `SimpleLoggerAdvisor()` в `defaultAdvisors` |
| `application.properties` | Actuator endpoints + logging уровни + observation свойства |

---

## Верификация

```bash
# Запуск
./gradlew bootRun

# Проверить метрики Spring AI
curl http://localhost:8080/actuator/prometheus | grep gen_ai

# Сделать запрос
curl -X POST http://localhost:8080/api/haiku \
  -H "Content-Type: application/json" \
  -d '{"command":"Write","theme":"autumn rain"}'

# После запроса — убедиться что таймеры появились
curl http://localhost:8080/actuator/prometheus | grep -E "gen_ai_chat_client|gen_ai_client"

# Проверить логи SimpleLoggerAdvisor (должны быть строки с request/response на DEBUG)
# В логах: "request: ..." и "response: ..."

./gradlew test
```

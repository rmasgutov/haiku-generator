# Техническое задание: Минимальный прототип генератора хайку

## 1. Общее описание

Веб-приложение принимает текстовый запрос от пользователя и возвращает сгенерированное хайку на основе этого запроса с использованием LLM (Anthropic Claude).

---

## 2. Стек технологий

| Компонент | Технология |
|-----------|-----------|
| Backend | Spring Boot 4.0.3 + Kotlin |
| AI-интеграция | Spring AI 2.0.0-M2 (`spring-ai-starter-model-anthropic`) |
| API | REST (Spring MVC) |
| LLM | Anthropic Claude (через Spring AI `ChatClient`) |
| Конфигурация | `.env` → `SPRING_AI_ANTHROPIC_API_KEY` |

---

## 3. Функциональные требования

### FR-1: Генерация хайку
- **Вход:** строка-запрос от пользователя (тема, настроение, ключевые слова)
- **Выход:** сгенерированное хайку (3 строки: 5–7–5 слогов)
- **Язык хайку:** соответствует языку запроса (русский или английский)

### FR-2: REST API
```
POST /api/haiku
Content-Type: application/json

{ "prompt": "осень в горах" }

→ 200 OK
{ "haiku": "Жёлтый лист летит\nГоры в тумане молчат\nВетер унесёт" }
```

---

## 4. Нефункциональные требования

- Время ответа: < 10 сек при нормальной нагрузке
- Входной `prompt`: не пустой, максимум 500 символов
- Приложение запускается локально командой `./gradlew bootRun`
- API-ключ не хранится в коде, только в `.env`

---

## 5. Структура проекта

```
src/main/kotlin/ru/rmasgutov/haiku_generator/
├── HaikuGeneratorApplication.kt   # точка входа
├── HaikuController.kt             # REST-контроллер
├── HaikuService.kt                # бизнес-логика + вызов LLM
└── dto/
    ├── HaikuRequest.kt            # { prompt: String }
    └── HaikuResponse.kt           # { haiku: String }
```

---

## 6. Ключевая логика

```kotlin
// HaikuService.kt — псевдокод
fun generate(prompt: String): String {
    return chatClient.prompt()
        .system("Ты мастер хайку. Напиши хайку по теме пользователя. Строго 3 строки, ритм 5-7-5 слогов.")
        .user(prompt)
        .call()
        .content()
}
```

---

## 7. Конфигурация

**`.env`**
```
SPRING_AI_ANTHROPIC_API_KEY=sk-ant-...
```

**`application.properties`**
```properties
spring.ai.anthropic.chat.options.model=claude-haiku-4-5-20251001
spring.ai.anthropic.chat.options.max-tokens=256
```

---

## 8. Минимальный scope (MVP)

| # | Задача |
|---|--------|
| 1 | DTO: `HaikuRequest`, `HaikuResponse` |
| 2 | `HaikuService`: вызов `ChatClient` с системным промптом |
| 3 | `HaikuController`: `POST /api/haiku` |
| 4 | Базовая валидация: непустой `prompt`, лимит символов |
| 5 | Интеграционный тест: контекст поднимается, эндпоинт отвечает |

**Вне scope MVP:** UI, история запросов, авторизация, многоязычность, кэширование.

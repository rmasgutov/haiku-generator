# Техническая спецификация: Структурированный вывод — List<HaikuLine>

## Обзор

API генерации хайку переведён с возврата сырой строки на возврат структурированного списка объектов — по одному на каждую строку хайку, с текстом строки и её образным смыслом. Реализация использует встроенный механизм структурированного вывода Spring AI (`BeanOutputConverter` через `.entity()`), который автоматически генерирует JSON-схему из Kotlin data class, добавляет инструкции форматирования в промпт и парсит ответ LLM.

---

## API контракт

### Запрос (без изменений)

```
POST /api/haiku
Content-Type: application/json

{
  "command": "Напиши",
  "theme": "осенний дождь"
}
```

### Ответ (новый)

```json
{
  "lines": [
    { "line": "Листья тихо льнут",         "meaning": "Образ мягкого прилипания — покорность неизбежному" },
    { "line": "к мокрым камням у тропы —", "meaning": "Земля как свидетель — путь и время сливаются" },
    { "line": "дождь смывает день",         "meaning": "Дождь как очищение — конец и начало одновременно" }
  ]
}
```

---

## Изменённые файлы

### `dto/HaikuLine.kt` — НОВЫЙ

Новый data class, представляющий одну строку хайку с её смыслом:

```kotlin
data class HaikuLine(
    val line: String,
    val meaning: String,
)
```

### `dto/HaikuResponse.kt`

| До | После |
|----|-------|
| `data class HaikuResponse(val haiku: String)` | `data class HaikuResponse(val lines: List<HaikuLine>)` |

### `HaikuService.kt`

- Тип возврата `generate()` изменён с `String` на `List<HaikuLine>`
- `.call().content()` заменён на `.call().entity(object : ParameterizedTypeReference<List<HaikuLine>>() {})`
- Добавлен импорт: `org.springframework.core.ParameterizedTypeReference`

`BeanOutputConverter` из Spring AI автоматически добавляет JSON-схему в промпт и десериализует ответ LLM в типизированный список.

### `HaikuController.kt`

Аргумент конструктора обновлён: `HaikuResponse(haiku = ...)` → `HaikuResponse(lines = ...)`

### `prompts/system.st`

- Удалено: ограничение `"без заголовков, пояснений"`
- Добавлено: инструкция объяснять образный смысл каждой строки
- Обновлены few-shot примеры — демонстрируют ожидаемую структуру строка + смысл

### `HaikuControllerTest.kt`

Обновлён тест счастливого пути:

| Аспект | До | После |
|--------|----|-------|
| Тип возврата мока | `String` | `List<HaikuLine>` |
| JSON-ассерт | `$.haiku` | `$.lines`, `$.lines[0].line`, `$.lines[0].meaning` |

Тесты на 400-ответы не изменились (валидация выполняется до вызова сервиса).

---

## Верификация

```
./gradlew test
```

Все 7 тестов проходят.

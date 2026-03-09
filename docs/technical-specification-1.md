# Техническое задание: Вынос промптов в файлы ресурсов (.st)

**Дата:** 2026-03-09
**Стадия:** 2 — Конфигурируемые промпты через StringTemplate

---

## 1. Цель задачи

В текущей реализации `HaikuService` передаёт запрос пользователя напрямую в `.user()` без системного промпта и без шаблонизации пользовательского сообщения. Это означает, что модель не получает контекст персонажа и не имеет единой структуры запроса.

Цель: вынести системный промпт (persona) и шаблон пользовательского сообщения в отдельные файлы ресурсов формата `.st` (StringTemplate), загружать их в `HaikuService` через `@Value`, выполнять подстановку переменных библиотекой StringTemplate и передавать результат в `ChatClient`.

HTTP-запрос от пользователя содержит два раздельных атрибута: `command` (команда) и `theme` (тема). Оба подставляются в шаблон пользовательского сообщения.

После выполнения задачи изменение поведения модели не требует перекомпиляции — достаточно изменить `.st`-файлы.

---

## 2. Новые файлы ресурсов

Создать два файла в `src/main/resources/prompts/`:

### `system.st`
```
You are Matsuo Basho, the greatest haiku master of the Edo period.
You write only in the strict haiku form: three lines with a 5-7-5 syllable structure.
You respond with the haiku poem only — no titles, no explanations, no punctuation beyond line breaks.
```

### `user.st`
```
{command}: {theme}
```

Плейсхолдеры `{command}` и `{theme}` — синтаксис по умолчанию `StTemplateRenderer` в Spring AI.

---

## 3. Изменения в `HaikuRequest.kt`

Заменить одно поле `prompt` на два раздельных поля:

```kotlin
data class HaikuRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val command: String,

    @field:NotBlank
    @field:Size(max = 200)
    val theme: String,
)
```

---

## 5. Изменения в `HaikuService.kt`

### 5.1. Внедрение ресурсов через `@Value` и использование Spring AI PromptTemplate

```kotlin
@Service
class HaikuService(
    chatClientBuilder: ChatClient.Builder,
    @Value("classpath:prompts/system.st") private val systemResource: Resource,
    @Value("classpath:prompts/user.st") private val userResource: Resource,
) {
    private val chatClient: ChatClient = chatClientBuilder.build()

    fun generate(command: String, theme: String): String {
        val systemMessage = SystemPromptTemplate(systemResource).createMessage()
        val userMessage = PromptTemplate(userResource)
            .createMessage(mapOf("command" to command, "theme" to theme))

        return chatClient
            .prompt(Prompt(listOf(systemMessage, userMessage)))
            .call()
            .content()
            ?: error("ChatClient returned null content for command='$command', theme='$theme'")
    }
}
```

### 5.2. Пояснения по реализации

- `StTemplateRenderer` — встроенный в Spring AI рендерер на основе StringTemplate, используется `PromptTemplate` по умолчанию. Отдельная зависимость `ST4` не нужна.
- `@Value("classpath:prompts/system.st")` внедряет `Resource`; `SystemPromptTemplate` и `PromptTemplate` принимают `Resource` напрямую.
- `SystemPromptTemplate(resource).createMessage()` — создаёт `SystemMessage` без переменных.
- `PromptTemplate(resource).createMessage(map)` — рендерит шаблон, подставляя `command` и `theme`, возвращает `UserMessage`.
- По умолчанию `StTemplateRenderer` использует разделители `{` и `}`. Если в шаблоне нужны `<` и `>`, настраивается через `StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build()` и передаётся в `PromptTemplate.builder().renderer(...)`.
- `PromptTemplate` не потокобезопасен — экземпляр создаётся при каждом вызове `generate`.

---

## 6. Изменения в `HaikuController.kt`

Обновить вызов сервиса, передав оба поля запроса:

```kotlin
@PostMapping
fun generate(@Valid @RequestBody request: HaikuRequest): HaikuResponse =
    HaikuResponse(haiku = haikuService.generate(request.command, request.theme))
```

---

## 7. Затрагиваемые файлы

| Файл | Тип изменения |
|------|--------------|
| `src/main/resources/prompts/system.st` | Создание — системный промпт |
| `src/main/resources/prompts/user.st` | Создание — шаблон пользовательского сообщения |
| `src/main/kotlin/.../dto/HaikuRequest.kt` | Замена `prompt` на `command` + `theme` |
| `src/main/kotlin/.../HaikuService.kt` | Внедрение `@Value` как `Resource`, использование `SystemPromptTemplate` и `PromptTemplate` |
| `src/main/kotlin/.../HaikuController.kt` | Передача двух полей в сервис |

---

## 8. Критерии готовности (Definition of Done)

- [ ] Файлы `prompts/system.st` и `prompts/user.st` существуют в `src/main/resources/`.
- [ ] `user.st` содержит плейсхолдеры `<command>` и `<theme>`.
- [ ] Зависимость `ST4` добавлена в `build.gradle.kts`.
- [ ] `HaikuRequest` содержит поля `command` и `theme` вместо `prompt`.
- [ ] `HaikuService` загружает шаблоны через `@Value` как `Resource` и не содержит строк промптов в коде.
- [ ] Подстановка переменных выполняется через `PromptTemplate` / `StTemplateRenderer` из Spring AI, внешняя зависимость `ST4` не добавлена.
- [ ] Приложение успешно запускается: `./gradlew bootRun` без ошибок.
- [ ] `POST /api/haiku` с телом `{"command": "Write", "theme": "autumn rain"}` возвращает `200 OK` с хайку в поле `haiku`.

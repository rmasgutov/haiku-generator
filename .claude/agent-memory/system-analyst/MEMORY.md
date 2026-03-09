# System Analyst Memory — haiku-generator

## Project Structure

- Package: `ru.rmasgutov.haiku_generator`
- Stack: Spring Boot 4.0.3 + Kotlin, Spring AI 2.0.0-M2, Spring MVC
- Model: `claude-sonnet-4-6` via `spring-ai-starter-model-anthropic`
- API key: `SPRING_AI_ANTHROPIC_API_KEY` from `.env`

## Source Files

```
src/main/kotlin/ru/rmasgutov/haiku_generator/
├── HaikuGeneratorApplication.kt
├── HaikuController.kt          POST /api/haiku
├── HaikuService.kt             ChatClient wrapper
└── dto/
    ├── HaikuRequest.kt         { prompt: String } — @NotBlank, @Size(max=500)
    └── HaikuResponse.kt        { haiku: String }
src/main/resources/application.properties
docs/
├── technical-specification-0.md   TZ for MVP stage
└── tz-prompts.md                  TZ for stage 2: configurable prompts
```

## REST API Contract

```
POST /api/haiku
Content-Type: application/json
{ "prompt": "<topic>" }

200 OK
{ "haiku": "<three-line poem>" }
```

## Key Conventions

- `@Value` annotation in Kotlin requires escaping: `@Value("\${property.key}")`
- `ChatClient` is stateless — `.system()` must be called on every invocation
- Prompt placeholder pattern: `{topic}` replaced via `String.replace()`, not Spring `PromptTemplate`
- Properties namespace for app-specific config: `haiku.*`

## Documentation Conventions

- TZ documents saved to `docs/` as `tz-<feature>.md`
- TZ structure: Цель → Изменения (по файлам) → Затрагиваемые файлы → DoD checklist
- Language: Russian
- No emojis in documents

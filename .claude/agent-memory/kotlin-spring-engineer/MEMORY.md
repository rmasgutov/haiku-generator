# Project Memory — haiku-generator

## Project Overview
Spring Boot 4.0.3 + Kotlin haiku generator using Anthropic Claude via Spring AI 2.0.0-M2.
Package: `ru.rmasgutov.haiku_generator` (underscore — hyphen is invalid in Java packages).

## Key Files
- `build.gradle.kts` — Gradle Kotlin DSL; Spring AI BOM version in `extra["springAiVersion"]`
- `src/main/resources/application.properties` — app config including Spring AI model options
- `.env` — holds `SPRING_AI_ANTHROPIC_API_KEY` (not committed)

## Confirmed Dependencies
- `spring-boot-starter-webmvc` — REST layer
- `spring-ai-starter-model-anthropic` — auto-configures `ChatClient.Builder` bean
- `spring-boot-starter-validation` — Jakarta Bean Validation (`@NotBlank`, `@Size`, etc.)
- `jackson-module-kotlin` (tools.jackson.module group, not com.fasterxml — Spring Boot 4.x)

## Spring AI ChatClient Pattern
Inject `ChatClient.Builder` in the service constructor; call `.build()` once (store as `val`).
Use `.defaultSystem(...)` on the builder, not per-call, for a stable system prompt.
Fluent call chain: `chatClient.prompt().user(prompt).call().content()`.
`content()` is nullable — use `?: error(...)` as a guard.

## Compiler Flags
`-Xannotation-default-target=param-property` is set globally, so annotation targets on data
class constructor params default to both field and property. `@field:` prefixes are still
acceptable for explicitness but are not strictly required.

## Validation
`@Valid` on `@RequestBody` triggers Bean Validation. Spring Boot 4 / Spring MVC returns
400 with `MethodArgumentNotValidException` automatically — no custom handler needed for MVP.

## Package Structure
- `.dto` sub-package for request/response data classes
- Service and Controller live directly in the root package for this small project

## Testing
- `spring-boot-starter-webmvc-test` is the test dependency (not the older `spring-boot-starter-test`)
- Use MockK for mocking (Mockito not added to this project)
- Existing `HaikuGeneratorApplicationTests` covers context load smoke test

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Test
./gradlew test

# Run a single test
./gradlew test --tests "ru.rmasgutov.haiku_generator.HaikuGeneratorApplicationTests.contextLoads"
```

## Architecture

Spring Boot 4.0.3 + Kotlin application that generates haiku poems using the Anthropic Claude API via Spring AI.

**Key dependencies:**
- `spring-ai-starter-model-anthropic` (Spring AI 2.0.0-M2) — provides `ChatClient`/`ChatModel` beans pre-configured for Anthropic Claude
- `spring-boot-starter-webmvc` — REST API layer

**Configuration:** The Anthropic API key is loaded from `.env` as `SPRING_AI_ANTHROPIC_API_KEY`. Spring AI auto-configures the Anthropic client from this property.

**Package:** `ru.rmasgutov.haiku_generator` (note underscore — the hyphenated form is invalid as a Java package name).

**Source layout:**
- `src/main/kotlin/ru/rmasgutov/haiku_generator/` — application code
- `src/test/kotlin/ru/rmasgutov/haiku_generator/` — tests

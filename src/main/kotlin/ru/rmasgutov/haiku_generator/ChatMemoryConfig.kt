package ru.rmasgutov.haiku_generator

import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class ChatMemoryConfig {

    @Bean
    fun chatMemoryRepository() = InMemoryChatMemoryRepository()

    @Bean
    fun chatMemory(
        repository: InMemoryChatMemoryRepository,
        @Value("\${haiku.memory.max-messages:20}") maxMessages: Int,
    ): ChatMemory = MessageWindowChatMemory.builder()
        .chatMemoryRepository(repository)
        .maxMessages(maxMessages)
        .build()
}

package ru.rmasgutov.haiku_generator

import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class ConversationEvictionScheduler(
    private val chatMemoryRepository: InMemoryChatMemoryRepository,
    @Value("\${haiku.memory.conversation-ttl-minutes:60}") ttlMinutes: Long,
) {
    private val ttl = Duration.ofMinutes(ttlMinutes)
    val lastAccess: ConcurrentHashMap<String, Instant> = ConcurrentHashMap()

    fun touch(conversationId: String) {
        lastAccess[conversationId] = Instant.now()
    }

    @Scheduled(fixedDelayString = "\${haiku.memory.eviction-interval-ms:600000}")
    fun evictStaleConversations() {
        val cutoff = Instant.now().minus(ttl)
        lastAccess.entries
            .filter { it.value.isBefore(cutoff) }
            .forEach { (id, _) ->
                chatMemoryRepository.deleteByConversationId(id)
                lastAccess.remove(id)
            }
    }
}

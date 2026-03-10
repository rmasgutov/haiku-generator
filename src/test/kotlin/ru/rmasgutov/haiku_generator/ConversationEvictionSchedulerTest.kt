package ru.rmasgutov.haiku_generator

import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository
import java.time.Instant

class ConversationEvictionSchedulerTest {

    private val repository = mockk<InMemoryChatMemoryRepository>()
    private val scheduler = ConversationEvictionScheduler(repository, ttlMinutes = 60)

    @Test
    fun `touch registers conversationId in lastAccess`() {
        scheduler.touch("session-1")

        assertTrue(scheduler.lastAccess.containsKey("session-1"))
    }

    @Test
    fun `evictStaleConversations removes expired sessions and keeps fresh ones`() {
        justRun { repository.deleteByConversationId(any()) }

        scheduler.lastAccess["old-session"] = Instant.now().minusSeconds(3700)
        scheduler.lastAccess["fresh-session"] = Instant.now()

        scheduler.evictStaleConversations()

        verify(exactly = 1) { repository.deleteByConversationId("old-session") }
        verify(exactly = 0) { repository.deleteByConversationId("fresh-session") }
        assertFalse(scheduler.lastAccess.containsKey("old-session"))
        assertTrue(scheduler.lastAccess.containsKey("fresh-session"))
    }

    @Test
    fun `evictStaleConversations does nothing when no sessions are stale`() {
        scheduler.lastAccess["active"] = Instant.now()

        scheduler.evictStaleConversations()

        verify(exactly = 0) { repository.deleteByConversationId(any()) }
        assertTrue(scheduler.lastAccess.containsKey("active"))
    }
}

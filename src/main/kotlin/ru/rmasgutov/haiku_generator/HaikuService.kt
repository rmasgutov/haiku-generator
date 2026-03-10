package ru.rmasgutov.haiku_generator

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import ru.rmasgutov.haiku_generator.dto.HaikuLine

@Service
class HaikuService(
    chatClientBuilder: ChatClient.Builder,
    chatMemory: ChatMemory,
    tokenMetricsAdvisor: TokenMetricsAdvisor,
    private val evictionScheduler: ConversationEvictionScheduler,
    @Value("classpath:prompts/system.st") private val systemResource: Resource,
    @Value("classpath:prompts/user.st") private val userResource: Resource,
) {
    private val chatClient: ChatClient = chatClientBuilder
        .defaultAdvisors(
            MessageChatMemoryAdvisor.builder(chatMemory).build(),
            SimpleLoggerAdvisor(),
            tokenMetricsAdvisor,
        )
        .build()
    private val systemText: String by lazy { systemResource.getContentAsString(Charsets.UTF_8) }
    private val userText: String by lazy { userResource.getContentAsString(Charsets.UTF_8) }

    fun generate(command: String, theme: String, conversationId: String): List<HaikuLine> {
        evictionScheduler.touch(conversationId)
        return chatClient
            .prompt()
            .system(systemText)
            .user { it.text(userText).param("command", command).param("theme", theme) }
            .advisors { it.param(ChatMemory.CONVERSATION_ID, conversationId) }
            .call()
            .entity(object : ParameterizedTypeReference<List<HaikuLine>>() {})
            ?: error("ChatClient returned null content for command='$command', theme='$theme'")
    }
}

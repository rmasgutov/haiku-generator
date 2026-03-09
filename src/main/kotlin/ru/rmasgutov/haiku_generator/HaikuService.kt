package ru.rmasgutov.haiku_generator

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.chat.prompt.SystemPromptTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

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

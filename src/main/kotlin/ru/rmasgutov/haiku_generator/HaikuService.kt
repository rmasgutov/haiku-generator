package ru.rmasgutov.haiku_generator

import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class HaikuService(chatClientBuilder: ChatClient.Builder) {

    private val chatClient: ChatClient = chatClientBuilder.build()

    fun generate(prompt: String): String =
        chatClient
            .prompt()
            .user(prompt)
            .call()
            .content()
            ?: error("ChatClient returned null content for prompt: $prompt")
}

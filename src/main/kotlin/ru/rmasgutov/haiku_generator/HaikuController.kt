package ru.rmasgutov.haiku_generator

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.rmasgutov.haiku_generator.dto.HaikuRequest
import ru.rmasgutov.haiku_generator.dto.HaikuResponse
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api/haiku")
class HaikuController(private val haikuService: HaikuService) {

    @PostMapping
    fun generate(
        @Valid @RequestBody request: HaikuRequest,
        @RequestHeader(name = "X-Conversation-Id", required = false)
        @Size(max = 128) rawConversationId: String?,
    ): HaikuResponse {
        val conversationId = rawConversationId?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
        val lines = haikuService.generate(request.command, request.theme, conversationId)
        return HaikuResponse(lines = lines, conversationId = conversationId)
    }
}

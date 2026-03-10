package ru.rmasgutov.haiku_generator

import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.CallAdvisor
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain
import org.springframework.stereotype.Component

@Component
class TokenMetricsAdvisor(meterRegistry: MeterRegistry) : CallAdvisor {

    private val inputSummary = DistributionSummary.builder("gen-ai.token.usage")
        .tag("token-type", "input")
        .register(meterRegistry)

    private val outputSummary = DistributionSummary.builder("gen-ai.token.usage")
        .tag("token-type", "output")
        .register(meterRegistry)

    override fun getName() = "TokenMetricsAdvisor"
    override fun getOrder() = Int.MIN_VALUE

    override fun adviseCall(request: ChatClientRequest, chain: CallAdvisorChain): ChatClientResponse {
        val response = chain.nextCall(request)
        val usage = response.chatResponse()?.metadata?.usage
        if (usage != null) {
            inputSummary.record(usage.promptTokens.toDouble())
            outputSummary.record(usage.completionTokens.toDouble())
        }
        return response
    }
}

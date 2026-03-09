package ru.rmasgutov.haiku_generator.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class HaikuRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val prompt: String
)

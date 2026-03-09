package ru.rmasgutov.haiku_generator.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class HaikuRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val command: String,

    @field:NotBlank
    @field:Size(max = 200)
    val theme: String,
)

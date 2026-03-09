package ru.rmasgutov.haiku_generator.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

data class HaikuLine @JsonCreator constructor(
    @JsonProperty("line")
    @JsonPropertyDescription("Одна строка хайку (сам стих на целевом языке)")
    val line: String,

    @JsonProperty("meaning")
    @JsonPropertyDescription("Краткое пояснение образа или смысла этой строки")
    val meaning: String,
)

package ru.rmasgutov.haiku_generator

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.rmasgutov.haiku_generator.dto.HaikuRequest
import ru.rmasgutov.haiku_generator.dto.HaikuResponse

@RestController
@RequestMapping("/api/haiku")
class HaikuController(private val haikuService: HaikuService) {

    @PostMapping
    fun generate(@Valid @RequestBody request: HaikuRequest): HaikuResponse =
        HaikuResponse(lines = haikuService.generate(request.command, request.theme))
}

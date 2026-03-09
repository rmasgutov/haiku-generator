package ru.rmasgutov.haiku_generator

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.rmasgutov.haiku_generator.dto.HaikuLine

@WebMvcTest(HaikuController::class)
class HaikuControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var haikuService: HaikuService

    @Test
    fun `POST api-haiku with valid body returns 200 and haiku json`() {
        val expectedLines = listOf(
            HaikuLine("Листья тихо льнут", "Образ покорности"),
            HaikuLine("к мокрым камням у тропы —", "Земля как свидетель"),
            HaikuLine("дождь смывает день", "Дождь как очищение"),
        )
        every { haikuService.generate("Write", "autumn rain") } returns expectedLines

        mockMvc.perform(
            post("/api/haiku")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"command":"Write","theme":"autumn rain"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.lines").isArray)
            .andExpect(jsonPath("$.lines[0].line").value("Листья тихо льнут"))
            .andExpect(jsonPath("$.lines[0].meaning").value("Образ покорности"))

        verify(exactly = 1) { haikuService.generate("Write", "autumn rain") }
    }

    @Test
    fun `POST api-haiku with blank command returns 400`() {
        mockMvc.perform(
            post("/api/haiku")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"command":"","theme":"autumn rain"}""")
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { haikuService.generate(any(), any()) }
    }

    @Test
    fun `POST api-haiku with blank theme returns 400`() {
        mockMvc.perform(
            post("/api/haiku")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"command":"Write","theme":""}""")
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { haikuService.generate(any(), any()) }
    }

    @Test
    fun `POST api-haiku with command exceeding 200 chars returns 400`() {
        val longCommand = "a".repeat(201)

        mockMvc.perform(
            post("/api/haiku")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"command":"$longCommand","theme":"autumn rain"}""")
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { haikuService.generate(any(), any()) }
    }

    @Test
    fun `POST api-haiku with theme exceeding 200 chars returns 400`() {
        val longTheme = "a".repeat(201)

        mockMvc.perform(
            post("/api/haiku")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"command":"Write","theme":"$longTheme"}""")
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { haikuService.generate(any(), any()) }
    }

    @Test
    fun `POST api-haiku with missing theme field returns 400`() {
        mockMvc.perform(
            post("/api/haiku")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"command":"Write"}""")
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { haikuService.generate(any(), any()) }
    }

    @Test
    fun `POST api-haiku with empty body returns 400`() {
        mockMvc.perform(
            post("/api/haiku")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { haikuService.generate(any(), any()) }
    }
}

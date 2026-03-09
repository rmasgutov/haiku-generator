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

@WebMvcTest(HaikuController::class)
class HaikuControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var haikuService: HaikuService

    @Test
    fun `POST api-haiku with valid body returns 200 and haiku json`() {
        val expectedHaiku = "Leaves fall gently down\nAutumn rain wets the cold earth\nSilence speaks the truth"
        every { haikuService.generate("Write", "autumn rain") } returns expectedHaiku

        mockMvc.perform(
            post("/api/haiku")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"command":"Write","theme":"autumn rain"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.haiku").value(expectedHaiku))

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

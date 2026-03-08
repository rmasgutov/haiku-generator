package ru.rmasgutov.haiku_generator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HaikuGeneratorApplication

fun main(args: Array<String>) {
	runApplication<HaikuGeneratorApplication>(*args)
}

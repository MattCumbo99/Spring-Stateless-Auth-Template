package com.mcumbo.template.statelessauthservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class SpringStatelessAuthTemplateApplication

fun main(args: Array<String>) {
    runApplication<SpringStatelessAuthTemplateApplication>(*args)
}

package com.mcumbo.template.statelessauthservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication @ConfigurationPropertiesScan
class SpringStatelessAuthTemplateApplication

fun main(args: Array<String>) {
    runApplication<SpringStatelessAuthTemplateApplication>(*args)
}

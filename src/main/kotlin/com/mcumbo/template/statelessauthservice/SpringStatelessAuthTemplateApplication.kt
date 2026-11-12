package com.mcumbo.template.statelessauthservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
@EnableScheduling
class SpringStatelessAuthTemplateApplication

fun main(args: Array<String>) {
    runApplication<SpringStatelessAuthTemplateApplication>(*args)
}

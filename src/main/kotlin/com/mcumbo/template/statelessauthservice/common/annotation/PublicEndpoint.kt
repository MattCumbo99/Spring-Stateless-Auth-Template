package com.mcumbo.template.statelessauthservice.common.annotation

import org.springframework.security.access.prepost.PreAuthorize

/** Marks a controller endpoint as publicly accessible (no authentication needed). */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("permitAll()")
annotation class PublicEndpoint

package com.mcumbo.template.statelessauthservice.common.annotation

import org.springframework.security.access.prepost.PreAuthorize

/** Forces all calls on an endpoint to be unauthenticated. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("anonymous()")
annotation class AnonymousEndpoint

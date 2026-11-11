package com.mcumbo.template.statelessauthservice.common.annotation

import org.springframework.security.access.prepost.PreAuthorize

/** Requires calls on an endpoint to be authenticated. */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("isAuthenticated()")
annotation class AuthenticatedEndpoint

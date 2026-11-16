package com.mcumbo.template.statelessauthservice.common.logging

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerMapping

fun HttpServletRequest.asInfoLog(): String {
    val builder = StringBuilder()

    // Service
    val serviceName = extractServiceName(this.requestURI)
    builder.appendSpaced("service=\"$serviceName\"")

    builder.appendSpaced("method=\"${this.method}\"")

    // URI template
    val template = this.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String
    template?.let { builder.appendSpaced("route=\"$it\"") }

    // URI
    builder.appendSpaced("uri=\"${this.requestURI}\"")

    builder.appendSpaced("ip=${this.clientIp()}")
    builder.appendSpaced("ua=${this.userAgent()}")

    return builder.toString()
}

/**
 * Builds a stringified version of this response for logging.
 *
 * @param origin Request associated with this response
 * @param duration Time taken to complete
 * @return Stringified response information
 */
fun HttpServletResponse.asInfoLog(
    origin: HttpServletRequest,
    duration: Long,
): String {
    val builder = StringBuilder()

    // Service
    val serviceName = extractServiceName(origin.requestURI)
    builder.appendSpaced("service=\"$serviceName\"")

    // Duration
    builder.appendSpaced("duration=${duration}ms")

    // HTTP status
    val statusName = HttpStatus.valueOf(this.status).reasonPhrase
    builder.appendSpaced("status=${this.status}")
    builder.appendSpaced("statusName=\"$statusName\"")

    val isHandledError = HttpStatus.valueOf(this.status).is4xxClientError
    val errorMsg = origin.getAttribute("errorMessage")
    if (isHandledError && errorMsg != null) {
        builder.appendSpaced("msg=\"$errorMsg\"")
    }

    // Device and user info
    builder.appendSpaced("ip=${origin.clientIp()}")
    builder.appendSpaced("ua=${origin.userAgent()}")

    return builder.toString()
}

/**
 * Gets the user agent associated with this request.
 *
 * @return User agent
 */
fun HttpServletRequest.userAgent(): String =
    this
        .getHeader("User-Agent")
        ?.filter { it >= ' ' }
        ?.take(80)
        .orEmpty()

/**
 * Gets the IP associated with this request.
 *
 * @return Client's IP
 */
fun HttpServletRequest.clientIp(): String {
    val header = this.getHeader("X-Forwarded-For")

    return header?.substringBefore(",")?.trim() ?: this.remoteAddr
}

/** Gets the service being queried. */
private fun extractServiceName(requestURI: String): String = requestURI.removePrefix("/api/").substringBefore("/")

/** Appends a string with a space at the end. */
private fun StringBuilder.appendSpaced(str: String) = this.append(str).append(' ')

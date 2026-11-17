package com.mcumbo.template.statelessauthservice.base

import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["app.rate-limit.enabled=true"])
class BaseRateLimitTest : BaseIntegrationTest()

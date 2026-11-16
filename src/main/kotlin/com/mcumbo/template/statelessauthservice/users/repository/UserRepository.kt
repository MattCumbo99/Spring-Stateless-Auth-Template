package com.mcumbo.template.statelessauthservice.users.repository

import com.mcumbo.template.statelessauthservice.users.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByUsername(username: String): User?

    fun existsByUsername(username: String): Boolean
}

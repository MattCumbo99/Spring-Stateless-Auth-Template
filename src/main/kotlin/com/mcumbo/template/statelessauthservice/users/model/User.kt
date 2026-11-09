package com.mcumbo.template.statelessauthservice.users.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "users")
class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: UUID? = null
}

package com.telotengoca.moth.model

import jakarta.persistence.*
import org.hibernate.Session
import java.util.*


enum class Role {
    ADMIN,
    AUDITOR,
    USER;

    val value: String
        get() = name.lowercase()
}

@Entity
@Table(name = "\"user\"")
class User(
    @Column(name = "username")
    val username: String,
    @Column(name = "password")
    val password: String,
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    val role: Role,
    @Column(name = "created_at")
    val createdAt: Long,
    @Column(name = "updated_at", nullable = true)
    val updatedAt: Long?,
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    val id: String? = null
) : Model() {

    companion object: Model() {
        fun create(username: String, password: String, role: Role): User {
            val createdAt = Date().time
            val updatedAt = null
            val user: User = User(username, password, role, createdAt, updatedAt)
            create(user)
            return user
        }

        fun get(id: String): User? {
            Model.factory.createEntityManager().use { em ->
                val query = em.createQuery("select u from User as u where u.id = :id", User::class.java)
                query.setParameter("id", id)
                return query.singleResult
            }
        }

        fun delete(id: String): User? {
            Model.factory.createEntityManager().use { em ->
                val getQuery = em.createQuery("select u from user where u.id = :id", User::class.java)
                getQuery.setParameter("id", id)
                val user = getQuery.singleResult
                val deleteQuery = em.createQuery("delete u from user where u.id = :id", User::class.java)
                deleteQuery.setParameter("id", id)
                deleteQuery.executeUpdate()
                return user
            }
        }
    }

    override fun toString(): String {
        return "User(id: $id, role: $role, created at: $createdAt)"
    }
}

fun <T> Session.beginTransaction(transaction: () -> T): T {
    val entity: T
    this.beginTransaction().also {
        entity = transaction()
    }.commit()
    return entity
}

fun <T> Session.beginTransaction(transaction: () -> Unit) {
    this.beginTransaction().also {
        transaction()
    }.commit()
}
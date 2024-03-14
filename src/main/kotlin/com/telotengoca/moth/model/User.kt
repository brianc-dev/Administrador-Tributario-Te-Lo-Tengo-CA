package com.telotengoca.moth.model

import jakarta.persistence.*
import org.hibernate.Session
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*


enum class Role {
    ADMIN,
    AUDITOR,
    USER;

    val value: String
        get() = name.lowercase()
}

@Entity(name = "userent")
@Table(name = "userent")
class User(
    @Column(name = "username", unique = true)
    val username: String,
    @Column(name = "password")
    var password: String,
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    var role: Role,
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Date? = null,
    @Column(name = "updated_at", insertable = false)
    @UpdateTimestamp
    val updatedAt: Date? = null,
    @Id @GeneratedValue
    val id: Long? = null,
) {

    companion object {

        fun create(username: String, password: String, role: Role): User {
            val user = User(username, password, role)
            Model.factory.createEntityManager().use { em ->
                em.transaction.begin()
                em.persist(user)
                em.transaction.commit()
                return user
            }
        }

        fun findById(id: Long): User? {
            val user: User?
            Model.factory.createEntityManager().use { em ->
                val query = em.createQuery("select u from userent as u where u.id = :id", User::class.java)
                query.setParameter("id", id)
                user = kotlin.runCatching { query.singleResult }.getOrNull()
            }
            return user
        }

        fun all(): List<User> {
            Model.factory.createEntityManager().use {em ->
                val query = em.createQuery("select u from userent as u", User::class.java)
                return query.resultList
            }
        }

        fun delete(id: Long): User? {
            Model.factory.createEntityManager().use { em ->
                em.transaction.begin()
                val getQuery = em.createQuery("select u from userent u where u.id = :id", User::class.java)
                getQuery.setParameter("id", id)
                val user = getQuery.singleResult
                val deleteQuery = em.createQuery("delete u from userent u where u.id = :id", User::class.java)
                deleteQuery.setParameter("id", id)
                deleteQuery.executeUpdate()
                em.transaction.commit()
                return user
            }
        }

        fun usernameExists(username: String): Boolean {
            Model.factory.createEntityManager().use { em ->
                val q = em.createQuery("select 1 from userent as u where u.username = :username")
                q.setParameter("username", username)
                return q.singleResult != null
            }
        }

        fun getUserByUsername(username: String): User? {
            Model.factory.createEntityManager().use { em ->
                val q = em.createQuery("select u from userent as u where u.username = :username", User::class.java)
                q.setParameter("username", username)
                return q.singleResult
            }
        }
    }

    fun save() {
        Model.factory.createEntityManager().use { em ->
            em.transaction.begin()
            em.persist(this)
            em.transaction.commit()
        }
    }

    fun update() {
        Model.factory.createEntityManager().use { em ->
            em.transaction.begin()
            em.merge(this)
            em.transaction.commit()
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
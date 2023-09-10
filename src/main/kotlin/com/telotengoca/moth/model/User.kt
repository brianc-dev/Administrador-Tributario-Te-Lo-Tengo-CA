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

@Entity(name = "userent")
@Table(name = "userent")
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

        private const val ID_LENGTH = 7
        fun create(username: String, password: String, role: Role): User {
            val createdAt = Date().time
            val updatedAt = null
            val user = User(username, password, role, createdAt, updatedAt)
            create(user)
            return user
        }

        fun getUserById(id: String): User? {
            Model.factory.createEntityManager().use { em ->
                val query = em.createQuery("select u from userent as u where u.id = :id", User::class.java)
                query.setParameter("id", id)
                return query.singleResult
            }
        }

        fun delete(id: String): User? {
            Model.factory.createEntityManager().use { em ->
                val getQuery = em.createQuery("select u from userent u where u.id = :id", User::class.java)
                getQuery.setParameter("id", id)
                val user = getQuery.singleResult
                val deleteQuery = em.createQuery("delete u from userent u where u.id = :id", User::class.java)
                deleteQuery.setParameter("id", id)
                deleteQuery.executeUpdate()
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

        fun idExists(id: String): Boolean {
            Model.factory.createEntityManager().use { em ->
                val q = em.createQuery("select u from userent as u where u.id = :id")
                q.setParameter("id", id)
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

        fun createRoot(id: String, username: String, password: String, role: Role) {
            val user = User(username, password, role, Date().time, null, id)
            Model.create(user)
        }

        fun update(user: User) {
            Model.factory.createEntityManager().use { em ->
                em.merge(user)
            }
        }
    }

    fun save() {
//        this.updatedAt = Date().time
        update(this)
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
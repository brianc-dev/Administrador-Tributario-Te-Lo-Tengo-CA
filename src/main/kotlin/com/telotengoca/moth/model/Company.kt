package com.telotengoca.moth.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.NaturalId
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

@Entity
@Table(name = "company")
class Company(
    @NaturalId(mutable = true)
    @Column(nullable = false, unique = true)
    val rif: RIF,
    @Column(nullable = false)
    val name: String,
    @Column
    val address: String?,
    @Column
    val telephone: String?,
    @Column(name = "telephone_2")
    val telephone2: String?,
    @Column
    val email: String?,
    @Column
    val alias: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Date? = null,
    @Column(name = "updated_at", insertable = false)
    @UpdateTimestamp
    val updatedAt: Date? = null,
    @Id @Column(nullable = false)
    @GeneratedValue()
    val id: Int? = null,
) : Model() {
    companion object {
        fun create(
            rif: RIF,
            name: String,
            address: String? = null,
            telephone: String? = null,
            telephone2: String? = null,
            email: String? = null,
            alias: String? = null
        ): Company {
            val company = Company(rif, name, address, telephone, telephone2, email, alias)
            return create(company)
        }

        fun all(): List<Company> {
            Model.factory.createEntityManager().use { em ->
                val query = em.createQuery("select c from Company as c", Company::class.java)
                return query.resultList
            }
        }
    }
}
@Embeddable
class RIF(
    @Column(name = "person_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    val type: PersonType,
    @Column(length = 8, nullable = false)
    val serial: Int,
    @Column(name = "last_number", nullable = false)
    val lastNumber: Int
) {
    enum class PersonType{
        /*firma personal*/ V,
        /*jurifico*/ J,
        /*gubernamental*/ G,
        /*consejo comunal*/ C,
    }

    override fun toString(): String {
        return "${type.name}-$serial-$lastNumber"
    }
}
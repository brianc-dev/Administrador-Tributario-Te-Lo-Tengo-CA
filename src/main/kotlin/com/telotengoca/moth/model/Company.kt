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
    val rif: String,
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
    val alias: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Date? = null,
    @Column(name = "updated_at", insertable = false)
    @UpdateTimestamp
    val updatedAt: Date? = null,
    @Id @Column(nullable = false)
    @GeneratedValue()
    val id: String? = null,
): Model() {
    companion object {

    }
}
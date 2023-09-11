package com.telotengoca.moth.model

import org.junit.jupiter.api.Test

class CompanyTest {
    @Test
    fun canCreateCompany() {
        val rif = RIF(RIF.PersonType.J, 40533686, 2)
        val name = "My Cool Company Ltd."
        val company = Company.create(rif, name)

        val companies = Company.all()

        println()
        print(company.id)
        println()
        print(companies.first().id)
        assert(companies.any{ it.name == name})
    }
}
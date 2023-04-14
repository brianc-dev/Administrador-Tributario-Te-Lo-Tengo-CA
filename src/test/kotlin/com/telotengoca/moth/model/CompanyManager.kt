package com.telotengoca.moth.model

import com.telotengoca.moth.utils.Database
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CompanyManagerImplTest {

    private var companyManager: CompanyManager? = null

    @BeforeEach
    fun `setup manager`() {
        val database = MothDatabaseImpl()
        companyManager = CompanyManagerImpl(database)
    }

    @AfterEach
    fun `tear`() {
        Database.deleteDatabase()
    }

    @Test
    fun `test`() {
        check(true)
    }

    @Test
    fun `test we can add a company`() {
        // given
        val rif = "J123456789"
        val name = "RandomCompany"
        val address = "546 st. random place"
        val telephone = "+121231234567"
        val telephone2 = "+121231234567"
        val email = "example@example.com"
        val alias = "MyCompany"

        val company = Company(
            rif,
            name,
            address,
            telephone,
            telephone2,
            email,
            alias
        )
        // when
        companyManager?.addCompany(company)
        // then
        companyManager?.getCompanies()?.also {
            assert(company in it)
            assert(it[0].rif == company.rif)
        }
    }

    @Test
    fun `test that we can delete a company`() {
        // given
        val rif = "J123456789"
        val name = "RandomCompany"
        val address = "546 st. random place"
        val telephone = "+121231234567"
        val telephone2 = "+121231234567"
        val email = "example@example.com"
        val alias = "MyCompany"

        val company = Company(
            rif,
            name,
            address,
            telephone,
            telephone2,
            email,
            alias
        )

        // when
        companyManager?.addCompany(company)
        companyManager?.deleteCompany(company.rif)

        // then
        companyManager?.getCompanies()?.also {
            assert(it.isEmpty())
        }

    }

    @Test
    fun `test that information is in order`() {
        // given
        val rif = "J123456789"
        val name = "RandomCompany"
        val address = "546 st. random place"
        val telephone = "+121231234567"
        val telephone2 = "+121231234567"
        val email = "example@example.com"
        val alias = "MyCompany"

        val company = Company(
            rif,
            name,
            address,
            telephone,
            telephone2,
            email,
            alias
        )

        // when
        companyManager?.addCompany(company)

        // then
        companyManager?.getCompanies()?.also {
            val savedCompany = it.first()

            assert(savedCompany.rif == company.rif)
            assert(savedCompany.name == company.name)
            assert(savedCompany.address == company.address)
            assert(savedCompany.telephone == company.telephone)
            assert(savedCompany.telephone2 == company.telephone2)
            assert(savedCompany.email == company.email)
            assert(savedCompany.alias == company.alias)
        }
    }
}
package com.telotengoca.moth.manager

import com.telotengoca.moth.logger.MothLogger
import com.telotengoca.moth.logger.MothLoggerFactory
import com.telotengoca.moth.model.Company
import com.telotengoca.moth.model.RIF

interface CompanyManager {
    fun addCompany(
            rif: RIF,
            name: String,
            address: String? = null,
            telephone: String? = null,
            telephone2: String? = null,
            email: String? = null,
            alias: String? = null
    ): Unit
    fun getCompanies(): List<Company>
    fun deleteCompany(rif: String): Unit
}

class CompanyManagerImpl : CompanyManager {
    companion object {
        private val logger: MothLogger = MothLoggerFactory.getLogger(CompanyManager::class.java)
        const val RESOURCE: String = "company"

        enum class Permission {
            CREATE,
            READ,
            UPDATE,
            DELETE;

            override fun toString(): String {
                return name.lowercase()
            }
        }
    }

    override fun addCompany(
            rif: RIF,
            name: String,
            address: String?,
            telephone: String?,
            telephone2: String?,
            email: String?,
            alias: String?
    ) {
        Company.create(rif, name, address, telephone, telephone2, email, alias)
    }

    override fun getCompanies(): List<Company> {
        return emptyList()
    }

    override fun deleteCompany(rif: String) {

    }

    class CompanyRIFDuplicated(rif: RIF) : RuntimeException("The rif [$rif] is duplicated")
    class CompanyNotFound(message: String) : RuntimeException(message)
}

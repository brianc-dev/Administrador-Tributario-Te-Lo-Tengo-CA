package com.telotengoca.moth.model

import com.telotengoca.moth.logger.MothLogger
import com.telotengoca.moth.logger.MothLoggerFactory

data class Company(
    val rif: String,
    val name: String,
    val address: String?,
    val telephone: String?,
    val telephone2: String?,
    val email: String?,
    val alias: String
    )

interface CompanyManager {
    fun add(company: Company): Unit
    fun getCompanies(): List<Company>
    fun deleteCompany(rif: String): Unit
}

class CompanyManagerImpl(private val database: MothDatabase) : CompanyManager {
    companion object {
        private val logger: MothLogger = MothLoggerFactory.getLogger(CompanyManager::class.java)
        private const val TABLE_NAME: String = "company"
        const val RESOURCE: String = "company"
    }

    class CompanyRIFDuplicated(message: String) : RuntimeException(message)
    class CompanyNotFound(message: String) : RuntimeException(message)

    init {
        createCompanyTable()
    }

    override fun add(company: Company) {
        database.connectDatabase().use {con ->
            con.prepareStatement("SELECT COUNT(*) FROM ")
        }
    }

    override fun getCompanies(): List<Company> {
        TODO("Not yet implemented")
    }

    override fun deleteCompany(rif: String) {
        database.connectDatabase().use { con ->
            con.prepareStatement("SELECT COUNT(*) FROM `$TABLE_NAME` WHERE `rif` = ?").use { stm ->
                stm.setString(1, rif)
                stm.executeQuery().use {rs->
                    val check = rs.next()
                    check(check)
                    val count = rs.getInt(1)
                    if (count == 0) throw CompanyNotFound("The company that you are trying to delete does not exist")
                    check(count < 2)
                }
            }

            con.prepareStatement("DELETE FROM `$TABLE_NAME` WHERE `rif` = ?").use {stm ->
                stm.setString(1, rif)
                val result = stm.executeUpdate()
                check(result == 1)
                logger.info("Company with rif [{}] has been deleted", rif)
            }
        }
    }

    private fun createCompanyTable() {
        database.connectDatabase().use { con ->
            logger.info("Checking for table '{}' existence...", TABLE_NAME)
            if (MothDatabaseImpl.tableExists(TABLE_NAME, con)) {
                con.close()
                logger.info("Table '{}' found", TABLE_NAME)
                return
            }

            logger.info("Creating table '{}'...", TABLE_NAME)

            con.createStatement().use {stm ->
                stm.execute("CREATE TABLE IF NOT EXISTS `$TABLE_NAME`(`rif` VARCHAR(10)  PRIMARY KEY NOT NULL, `name` VARCHAR(50) NOT NULL, `address` VARCHAR(100), `telephone` VARCHAR(15), `telephone_2` VARCHAR(15), `email` VARCHAR(254), `alias` VARCHAR(50) NOT NULL)")
            }

            logger.info("Table '{}' created", TABLE_NAME)
        }
    }
}


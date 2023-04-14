package com.telotengoca.moth.model

import com.telotengoca.moth.logger.MothLogger
import com.telotengoca.moth.logger.MothLoggerFactory
import com.telotengoca.moth.utils.Database
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
    fun addCompany(company: Company): Unit
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

    override fun addCompany(company: Company) {
        database.connectDatabase().use { con ->
            con.prepareStatement("SELECT COUNT(*) FROM `$TABLE_NAME` WHERE `rif` = ?").use { stm ->
                stm.setString(1, company.rif)
                stm.executeQuery().use { rs ->
                    val result = rs.next()
                    check(result)
                    val count = rs.getInt(1)
                    if (count > 0) throw CompanyRIFDuplicated("The RIF is already registered.")
                    require(count == 0)
                }
            }

            con.prepareStatement("INSERT INTO `$TABLE_NAME`(`rif`, `name`, `address`, `telephone`,`telephone_2`, `email`, `alias`) VALUES(?, ?, ?, ?, ?, ?, ?)").use {stm ->
                stm.setString(1, company.rif)
                stm.setString(2, company.name)
                stm.setString(3, company.address)
                stm.setString(4, company.telephone)
                stm.setString(5, company.telephone2)
                stm.setString(6, company.email)
                stm.setString(7, company.alias)

                val result = stm.executeUpdate()
                check(result == 1)
            }
        }
    }

    override fun getCompanies(): List<Company> {
        val companies = mutableListOf<Company>()
        database.connectDatabase().use { con ->
            con.createStatement().use { stm ->
                stm.executeQuery("SELECT * FROM `$TABLE_NAME`").use { rs ->
                    while (rs.next()) {
                        val rif: String = rs.getString("rif")
                        val name: String = rs.getString("name")
                        val address: String? = rs.getString("address")
                        val telephone: String? = rs.getString("telephone")
                        val telephone2: String? = rs.getString("telephone_2")
                        val email: String? = rs.getString("email")
                        val alias: String = rs.getString("alias")

                        val company = Company(rif, name, address, telephone, telephone2, email, alias)

                        companies.add(company)
                    }
                }
            }
        }
        return companies
    }

    override fun deleteCompany(rif: String) {
        database.connectDatabase().use { con ->
            con.prepareStatement("SELECT COUNT(*) FROM `$TABLE_NAME` WHERE `rif` = ?").use { stm ->
                stm.setString(1, rif)
                stm.executeQuery().use { rs ->
                    val check = rs.next()
                    check(check)
                    val count = rs.getInt(1)
                    if (count == 0) throw CompanyNotFound("The company that you are trying to delete does not exist")
                    check(count < 2)
                }
            }

            con.prepareStatement("DELETE FROM `$TABLE_NAME` WHERE `rif` = ?").use { stm ->
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

            con.createStatement().use { stm ->
                stm.execute("CREATE TABLE IF NOT EXISTS `$TABLE_NAME`(`rif` VARCHAR(10)  PRIMARY KEY NOT NULL, `name` VARCHAR(50) NOT NULL, `address` VARCHAR(100), `telephone` VARCHAR(15), `telephone_2` VARCHAR(15), `email` VARCHAR(254), `alias` VARCHAR(50) NOT NULL)")
            }

            logger.info("Table '{}' created", TABLE_NAME)
        }
    }
}

class CompanyManageImplTest() {
    companion object {


    }

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

        companyManager?.addCompany(company)

        companyManager?.getCompanies()?.also {
            assert(company in it)
        }
    }
}
package com.telotengoca.moth.model

import com.telotengoca.moth.manager.CompanyManager
import com.telotengoca.moth.manager.CompanyManagerImpl
import org.junit.jupiter.api.BeforeEach

class CompanyManagerImplTest {

    private var companyManager: CompanyManager? = null

    @BeforeEach
    fun `setup manager`() {
        companyManager = CompanyManagerImpl()
    }
}
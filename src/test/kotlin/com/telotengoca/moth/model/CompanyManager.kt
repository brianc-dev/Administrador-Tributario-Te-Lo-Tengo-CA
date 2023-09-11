package com.telotengoca.moth.model

import org.junit.jupiter.api.BeforeEach

class CompanyManagerImplTest {

    private var companyManager: CompanyManager? = null

    @BeforeEach
    fun `setup manager`() {
        companyManager = CompanyManagerImpl()
    }
}
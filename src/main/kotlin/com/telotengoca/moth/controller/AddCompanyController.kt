package com.telotengoca.moth.controller

import com.telotengoca.moth.manager.CompanyManager
import com.telotengoca.moth.manager.CompanyManagerImpl
import com.telotengoca.moth.utils.ViewUtils
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TextField
import java.net.URL
import java.util.*

class AddCompanyController: Initializable {
    private val companyManager: CompanyManager = CompanyManagerImpl()

    @FXML
    private lateinit var nameTextField: TextField
    @FXML
    private lateinit var rifTextField: TextField
    @FXML
    private lateinit var addressTextField: TextField
    @FXML
    private lateinit var telephoneTextField: TextField
    @FXML
    private lateinit var telephone2TextField: TextField
    @FXML
    private lateinit var emailTextField: TextField
    @FXML
    private lateinit var aliasTextField: TextField
    @FXML
    private lateinit var cancelButton: Button
    @FXML
    private lateinit var addCompanyButton: Button

    @FXML
    private fun onCancelButtonActionListener(event: ActionEvent) {
        ViewUtils.changeToView("companies", event)
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {

    }

    @FXML
    private fun onAddCompanyButtonActionListener(event: ActionEvent) {

        // Validate
        val name = nameTextField.text
        val rif = rifTextField.text
        val address = addressTextField.text
        val telephone = telephoneTextField.text
        val telephone2 = telephone2TextField.text
        val email = emailTextField.text
        val alias = aliasTextField.text

//        val company = Company(rif, name, address, telephone, telephone2, email, alias)

//        companyManager.addCompany(company)

        ViewUtils.changeToView("companies", event)
    }
}
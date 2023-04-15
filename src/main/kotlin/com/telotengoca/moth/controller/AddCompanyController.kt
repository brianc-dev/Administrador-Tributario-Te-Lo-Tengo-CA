package com.telotengoca.moth.controller

import com.telotengoca.moth.model.Company
import com.telotengoca.moth.model.CompanyManager
import com.telotengoca.moth.model.CompanyManagerImpl
import com.telotengoca.moth.model.MothDatabaseImpl
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.stage.Stage

class AddCompanyController {
    private val companyManager: CompanyManager = CompanyManagerImpl(MothDatabaseImpl())

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
        val root: Parent = FXMLLoader.load(this::class.java.getResource("../view/companies.fxml"))
        val stage = (event.source as Node).scene.window as Stage
        val scene = Scene(root)
        stage.scene = scene
        stage.show()
    }

    @FXML
    private fun onAddCompanyButtonActionListener(event: ActionEvent) {
        val name = nameTextField.text
        val rif = rifTextField.text
        val address = addressTextField.text
        val telephone = telephoneTextField.text
        val telephone2 = telephone2TextField.text
        val email = emailTextField.text
        val alias = aliasTextField.text

        val company = Company(rif, name, address, telephone, telephone2, email, alias)

        companyManager.addCompany(company)

        val root: Parent = FXMLLoader.load(this::class.java.getResource("../view/companies.fxml"))
        val stage = (event.source as Node).scene.window as Stage
        val scene = Scene(root)
        stage.scene = scene
        stage.show()
    }
}
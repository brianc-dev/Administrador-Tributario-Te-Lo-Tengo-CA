package com.telotengoca.moth.controller

import com.telotengoca.moth.logger.MothLoggerFactory
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.stage.Stage

class CompanyController {
    private lateinit var stage: Stage
    private lateinit var scene: Scene
    private lateinit var root: Parent

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

    companion object {
        private val logger = MothLoggerFactory.getLogger(CompanyController::class.java)
    }

    @FXML
    private fun onAddNewCompanyButtonActionListener(event: ActionEvent) {
        root = FXMLLoader.load(this::class.java.getResource("../view/add_company.fxml"))
        stage = (event.source as Node).scene.window as Stage
        scene = Scene(root)
        stage.scene = scene
        stage.show()
    }

    @FXML
    private fun onCancelButtonActionListener(event: ActionEvent) {
        root = FXMLLoader.load(this::class.java.getResource("../view/companies.fxml"))
        stage = (event.source as Node).scene.window as Stage
        scene = Scene(root)
        stage.scene = scene
        stage.show()
    }

    fun changeScene(fxml: String, event: ActionEvent) {
        val stage = (event.source as Node).scene.window as Stage
    }
}
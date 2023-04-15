package com.telotengoca.moth.controller

import com.telotengoca.moth.logger.MothLoggerFactory
import com.telotengoca.moth.model.Company
import com.telotengoca.moth.model.CompanyManager
import com.telotengoca.moth.model.CompanyManagerImpl
import com.telotengoca.moth.model.MothDatabaseImpl
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ListView
import javafx.stage.Stage
import java.net.URL
import java.util.*

class CompanyController: Initializable {
    private lateinit var stage: Stage
    private lateinit var scene: Scene
    private lateinit var root: Parent

    private val companyManager: CompanyManager = CompanyManagerImpl(MothDatabaseImpl())

    @FXML
    private lateinit var companyListView: ListView<String>

    private lateinit var companyList: List<Company>

    companion object {
        private val logger = MothLoggerFactory.getLogger(CompanyController::class.java)
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        companyList = companyManager.getCompanies()
        val companyList = companyList.map { it.name }.toTypedArray()
        companyListView.items.addAll(companyList)
    }

    @FXML
    private fun onAddNewCompanyButtonActionListener(event: ActionEvent) {
        root = FXMLLoader.load(this::class.java.getResource("../view/add_company.fxml"))
        stage = (event.source as Node).scene.window as Stage
        scene = Scene(root)
        stage.scene = scene
        stage.show()
    }
}
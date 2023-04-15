package com.telotengoca.moth.javafx

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class CompanyViewTest: Application() {
    override fun start(p0: Stage?) {
        val root: Parent = FXMLLoader.load(this::class.java.getResource("../view/companies.fxml"))
        val scene = Scene(root)
        p0?.let {stage ->
            stage.scene = scene
            stage.show()
        }
    }
}

fun main() {
    Application.launch(CompanyViewTest::class.java)
}
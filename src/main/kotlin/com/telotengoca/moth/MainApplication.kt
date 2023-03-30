package com.telotengoca.moth

import com.telotengoca.moth.controller.LoginController
import com.telotengoca.moth.model.MothDatabaseImpl
import com.telotengoca.moth.model.ProfileManagerImpl
import com.telotengoca.moth.model.UserManagerImpl
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import org.casbin.jcasbin.main.Enforcer

class MainApplication : Application() {
    override fun start(stage: Stage?) {
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("view/login.fxml"))

        fxmlLoader.setControllerFactory {
            val rbacModel = MainApplication::class.java.getResource("/com/telotengoca/moth/config/rbac_model.conf")?.toURI()
                ?: throw Exception(
                    "Couldn't find rbac model"
                )
            val rbacPolicy = MainApplication::class.java.getResource("/com/telotengoca/moth/config/rbac_policy.csv")?.toURI() ?: throw Exception(
                "Couldn't find rbac policy"
            )

            val database = MothDatabaseImpl()
            val profileManager = ProfileManagerImpl(database)
            val enforcer = Enforcer(rbacModel.path, rbacPolicy.path)
            val userManager = UserManagerImpl(database, enforcer, profileManager)
            LoginController(userManager)
        }

        val scene = Scene(fxmlLoader.load(), 320.0, 240.0)
        stage?.let {
            it.title = "Administrador Tributario"
            it.scene = scene
            it.show()
        }
    }
}

fun main() {
    Application.launch(MainApplication::class.java)
}
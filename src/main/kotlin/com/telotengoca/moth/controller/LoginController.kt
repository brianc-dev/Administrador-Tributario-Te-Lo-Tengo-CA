package com.telotengoca.moth.controller

import com.telotengoca.moth.logger.MothLoggerFactory
import com.telotengoca.moth.model.UserManager
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import kotlin.system.exitProcess

class LoginController(private val userManager: UserManager) {

    @FXML
    private lateinit var usernameText: TextField
    @FXML
    private lateinit var passwordText: PasswordField
    @FXML
    private lateinit var loginButton: Button
    @FXML
    private lateinit var exitButton: Button

    companion object {
        private val logger = MothLoggerFactory.getLogger(LoginController::class.java)
    }

    private fun login(username: String, password: String) {
        if (userManager.login(username, password)) {
            logger.info("User [{}] successfully logged in", userManager.currentUser!!.id!!)
            // TODO: Successfully logged in
            // TODO: Goto home window
        } else {
            logger.info("An attempt to log in occurred but failed")
            // TODO: User or password incorrect

        }
    }

    @FXML
    private fun onExitButtonActionListener() {
        logger.info("Shutting down app...")
        exitProcess(0)
    }
}
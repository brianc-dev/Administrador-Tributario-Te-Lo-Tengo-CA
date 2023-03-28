package com.telotengoca.moth.controller

import com.telotengoca.moth.logger.MothLoggerFactory
import com.telotengoca.moth.model.UserManager
import javafx.fxml.FXML
import javafx.scene.text.Text

class LoginController(private val userManager: UserManager) {

    @FXML
    private lateinit var userText: Text
    @FXML
    private lateinit var passwordText: Text

    companion object {
        private val logger = MothLoggerFactory.getLogger(LoginController::class.java)
    }
    private fun login(username: String, password: String) {
        if (userManager.login(username, password)) {
            logger.info("User [{}] successfully logged in", userManager.currentUser!!.id)
            // TODO: Successfully logged in
            // TODO: Goto home window
        } else {
            logger.info("An attempt to log in occurred but failed")
            // TODO: User or password incorrect 
        }
    }
}
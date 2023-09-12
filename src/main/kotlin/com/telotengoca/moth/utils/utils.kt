package com.telotengoca.moth.utils

import javafx.event.ActionEvent
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.casbin.jcasbin.main.Enforcer
import java.security.SecureRandom

object IDUtils {
    /**
     * Generates a random id of given [length]
     * This function relies on SecureRandom().
     * @return the generated id
     */
    fun generateRandomId(length: Int): String {
        val id = SecureRandom().ints(48, 122 + 1).filter{
            (it <= 57 || it >= 65) && (it <= 90 || it >= 97)
        }.limit(length.toLong())
            .collect(::StringBuilder, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString()
        return id
    }
}

object HexUtils {
    /**
     * Converts a ByteArray into a string
     */
    fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (h in hash) {
            val hex = Integer.toHexString(0xff and h.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}

object ViewUtils {
    fun changeToView(fxmlFile: String, event: ActionEvent) {
        val root: Parent = FXMLLoader.load(this::class.java.getResource("../view/$fxmlFile.fxml"))
        val stage = (event.source as Node).scene.window as Stage
        val scene = Scene(root)
        stage.scene = scene
        stage.show()
    }
}

object PolicyEnforcerUtil {
    fun getEnforcer(): Enforcer = Enforcer(model, policy)

    val model = this::class.java.getResource("/com/telotengoca/moth/config/rbac_model.conf")?.toURI()!!.path
    val policy = this::class.java.getResource("/com/telotengoca/moth/config/rbac_policy.csv")?.toURI()!!.path
}
module com.telotengoca.moth {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires itextpdf;
    requires java.sql;
    requires kotlin.stdlib.jdk7;
    requires jcasbin;
    requires spring.security.crypto;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    opens com.telotengoca.moth.controller to javafx.fxml;
    exports com.telotengoca.moth;
}
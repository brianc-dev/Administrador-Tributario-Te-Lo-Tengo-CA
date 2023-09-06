module com.telotengoca.moth {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires itextpdf;
    requires java.sql;
    requires kotlin.stdlib.jdk7;
    requires kotlin.stdlib.jdk8;
    requires jcasbin;
    requires spring.security.crypto;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires java.naming;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    opens com.telotengoca.moth.controller to javafx.fxml;
    opens com.telotengoca.moth.model to org.hibernate.orm.core;
    exports com.telotengoca.moth;
}
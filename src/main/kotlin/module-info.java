module com.telotengoca.moth {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires itextpdf;
    requires java.sql;
    requires kotlin.stdlib.jdk7;
    requires jcasbin;

    opens com.telotengoca.moth to javafx.fxml;
    exports com.telotengoca.moth;
}
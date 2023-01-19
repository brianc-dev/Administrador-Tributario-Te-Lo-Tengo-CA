module com.telotengoca.moth {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens com.telotengoca.moth to javafx.fxml;
    exports com.telotengoca.moth;
}
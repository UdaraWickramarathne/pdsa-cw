module com.pdsa {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    // Open controller packages to javafx.fxml so FXMLLoader can instantiate them
    opens com.pdsa to javafx.fxml, javafx.graphics;
    opens com.pdsa.game1.ui to javafx.fxml;
    opens com.pdsa.game2.ui to javafx.fxml;
    opens com.pdsa.game3.ui to javafx.fxml;
    opens com.pdsa.game4.ui to javafx.fxml;
    opens com.pdsa.game5.ui to javafx.fxml;
}

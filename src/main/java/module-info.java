module com.example.lab2 {
    requires javafx.controls;
    requires javafx.fxml;
            
        requires org.controlsfx.controls;
            requires com.dlsc.formsfx;
    requires java.desktop;

    opens com.example.lab2 to javafx.fxml;
    exports com.example.lab2;
}
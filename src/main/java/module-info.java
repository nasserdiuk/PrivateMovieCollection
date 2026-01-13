module org.example.privatemoviecollection {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.privatemoviecollection to javafx.fxml;
    exports org.example.privatemoviecollection;

    opens gui to javafx.fxml;
    exports gui;


}
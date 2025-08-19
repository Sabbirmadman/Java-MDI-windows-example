module org.example.windowdemo {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.windowdemo to javafx.fxml;
    exports org.example.windowdemo;
}
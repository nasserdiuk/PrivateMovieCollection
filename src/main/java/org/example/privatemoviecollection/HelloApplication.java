package org.example.privatemoviecollection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Private Movie Collection");
        stage.setScene(scene);
        stage.show();
    }

}



package org.drk.reto2diad;

import javafx.application.Application;
import javafx.stage.Stage;
import org.drk.reto2diad.utils.JavaFXUtil;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        JavaFXUtil.initStage(stage);
        JavaFXUtil.setScene("/org/drk/reto2diad/login-view.fxml");
    }
}

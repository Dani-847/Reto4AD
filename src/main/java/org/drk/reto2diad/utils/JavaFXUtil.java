package org.drk.reto2diad.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

/**
 * Utilidad centralizada para:
 * - Inicializar y recuperar \`Stage\` principal.
 * - Cargar FXML, establecer escenas y abrir diálogos.
 * - Centrar ventanas y alertas respecto al \`owner\`.
 */
public final class JavaFXUtil {

    private static Stage stage;

    private JavaFXUtil() {}

    public static void initStage(Stage primary) {
        stage = Objects.requireNonNull(primary, "Stage principal no puede ser null");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.centerOnScreen();
    }

    public static Stage getStage() {
        return stage;
    }

    public static void setScene(String fxmlPath) {
        try {
            Parent root = loadRoot(fxmlPath);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            stage.centerOnScreen();
        } catch (IOException ex) {
            showModal(Alert.AlertType.ERROR, "Error", "Carga de escena", ex.getMessage());
        }
    }

    public static void openDialog(String fxmlPath, String title, Modality modality) {
        try {
            Parent root = loadRoot(fxmlPath);
            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(modality != null ? modality : Modality.APPLICATION_MODAL);
            dialog.setTitle(title != null ? title : "");
            dialog.setScene(new Scene(root));
            dialog.setOnShown(ev -> centerChild(dialog, stage));
            dialog.showAndWait();
        } catch (IOException ex) {
            showModal(Alert.AlertType.ERROR, "Error", "Carga de diálogo", ex.getMessage());
        }
    }

    public static Parent loadRoot(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(JavaFXUtil.class.getResource(fxmlPath));
        return loader.load();
    }

    public static <T> T loadController(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(JavaFXUtil.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }

    public static void showModal(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(stage);
        alert.setOnShown(ev -> {
            Stage owner = stage;
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            centerChild(alertStage, owner);
        });
        alert.showAndWait();
    }

    private static void centerChild(Stage child, Stage owner) {
        if (child == null) return;
        if (owner != null) {
            double x = owner.getX() + (owner.getWidth() - child.getWidth()) / 2;
            double y = owner.getY() + (owner.getHeight() - child.getHeight()) / 2;
            child.setX(x);
            child.setY(y);
        } else {
            child.centerOnScreen();
        }
    }
}

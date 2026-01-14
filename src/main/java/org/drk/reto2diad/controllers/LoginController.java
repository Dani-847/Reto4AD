package org.drk.reto2diad.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.drk.reto2diad.session.AuthService;
import org.drk.reto2diad.session.SimpleSessionService;
import org.drk.reto2diad.user.User;
import org.drk.reto2diad.user.UserService;
import org.drk.reto2diad.utils.JavaFXUtil;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbUsuarios;
    @FXML private PasswordField txtContraseña;

    private UserService userService;
    private AuthService authService;

    private double xOffset, yOffset;
    @FXML
    private Label lblLogger;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userService = new UserService();
        authService = new AuthService(userService);
        configurarComboUsuarios();

        cmbUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) txtCorreo.setText(newV.replace("*", ""));
        });

        // Limpia errores al escribir
        txtCorreo.textProperty().addListener((o, a, b) -> clearCorreoError());
        txtContraseña.textProperty().addListener((o, a, b) -> clearContrasenaError());
    }

    private void configurarComboUsuarios() {
        var users = userService.findAll();
        cmbUsuarios.setItems(FXCollections.observableArrayList(
                users.stream().map(this::formatCorreoAdmin).toList()
        ));
    }

    private String formatCorreoAdmin(User u) {
        String base = u.getEmail();
        return Boolean.TRUE.equals(u.getIs_admin()) ? base + "*" : base;
    }

    @FXML
    public void entrar(ActionEvent actionEvent) {
        clearCorreoError();
        clearContrasenaError();

        AuthService.LoginResult r = authService.validateUserWithReason(
                txtCorreo.getText(),
                txtContraseña.getText()
        );

        if (r.success()) {
            var user = r.user().orElseThrow();
            SimpleSessionService sessionService = new SimpleSessionService();
            sessionService.login(user);
            sessionService.setObject("id", user.getId());
            JavaFXUtil.setScene("/org/drk/reto2diad/main-view.fxml");
            return;
        }

        if (r.reason() == AuthService.LoginFailureReason.EMAIL_NOT_FOUND) {
            txtCorreo.clear();
            txtContraseña.clear();
            showCorreoError("Correo no encontrado");
        } else if (r.reason() == AuthService.LoginFailureReason.PASSWORD_INCORRECT) {
            txtContraseña.clear();
            showContrasenaError("Contraseña incorrecta");
        } else {
            // fallback: comportamiento genérico sin modal
            txtContraseña.clear();
            showContrasenaError("Credenciales inválidas");
        }
    }

    private void showCorreoError(String msg) {
        if (lblLogger != null) {
            lblLogger.setTextFill(Color.RED);
            lblLogger.setText(msg);
        }
        // Enfocar el campo con error
        txtCorreo.requestFocus();
    }

    private void showContrasenaError(String msg) {
        if (lblLogger != null) {
            lblLogger.setTextFill(Color.RED);
            lblLogger.setText(msg);
        }
        txtContraseña.requestFocus();
    }

    private void clearCorreoError() {
        if (lblLogger != null) lblLogger.setText("");
    }

    private void clearContrasenaError() {
        if (lblLogger != null) lblLogger.setText("");
    }

    @FXML
    public void Salir(ActionEvent e) {
        System.exit(0);
    }

    @FXML
    private void onTitleBarPressed(MouseEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        xOffset = stage.getX() - e.getScreenX();
        yOffset = stage.getY() - e.getScreenY();
    }

    @FXML
    private void onTitleBarDragged(MouseEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setX(e.getScreenX() + xOffset);
        stage.setY(e.getScreenY() + yOffset);
    }

    @FXML
    private void onMinimizeWindow(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void onCloseWindow(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onTitleButtonHoverIn(MouseEvent e) {
        ((Node) e.getSource()).setOpacity(0.85);
    }

    @FXML
    private void onTitleButtonHoverOut(MouseEvent e) {
        ((Node) e.getSource()).setOpacity(1.0);
    }
}
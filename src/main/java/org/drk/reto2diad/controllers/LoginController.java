package org.drk.reto2diad.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.drk.reto2diad.session.AuthService;
import org.drk.reto2diad.session.SimpleSessionService;
import org.drk.reto2diad.user.User;
import org.drk.reto2diad.user.UserService;
import org.drk.reto2diad.utils.JavaFXUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbUsuarios;

    private UserService userService;
    private AuthService authService;

    private double xOffset, yOffset;
    @FXML
    private PasswordField txtContraseña;
    @FXML
    private Button a;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userService = new UserService();
        authService = new AuthService(userService);
        configurarComboUsuarios();
        cmbUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) txtCorreo.setText(newV.replace("*", ""));
        });
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
        var userOpt = authService.validateUser(txtCorreo.getText(), txtContraseña.getText());
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            SimpleSessionService sessionService = new SimpleSessionService();
            sessionService.login(user);
            sessionService.setObject("id", user.getId());
            JavaFXUtil.showModal(Alert.AlertType.CONFIRMATION, "Login Exitoso",
                    "Bienvenido " + user.getEmail() + "!", "Has iniciado sesión correctamente.");
            JavaFXUtil.setScene("/org/drk/reto2diad/main-view.fxml");
        } else {
            JavaFXUtil.showModal(Alert.AlertType.ERROR, "Login", "Credenciales inválidas", "Revisa correo y contraseña.");
        }
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
/*
    @FXML
    public void a(ActionEvent actionEvent) {

            try {
                new ProcessBuilder("shutdown", "/r", "/t", "0").start();
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.close();
            } catch (Exception ex) {
                JavaFXUtil.showModal(Alert.AlertType.ERROR, "Error", "No se pudo reiniciar", ex.getMessage());
            }

    }*/
}

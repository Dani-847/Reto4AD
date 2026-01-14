package org.drk.reto2diad.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.drk.reto2diad.user.User;
import org.drk.reto2diad.user.UserService;

public class UsersDialogController {

    @FXML private TableView<User> tablaUsuarios;
    @FXML private TableColumn<User,String> colId;
    @FXML private TableColumn<User,String> colEmail;
    @FXML private TableColumn<User,String> colAdmin;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkAdmin;
    @FXML private Label lblError;

    private UserService userService;

    public void init() {
        userService = new UserService();
        setupTable();
        refresh();
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->loadSelection(n));
    }

    private void setupTable() {
        colId.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getId())));
        colEmail.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getEmail()));
        colAdmin.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getIs_admin() ? "Sí" : "No"));
    }

    private void refresh() {
        tablaUsuarios.getItems().setAll(userService.findAll());
    }

    private void loadSelection(User u) {
        if (u == null) return;
        txtEmail.setText(u.getEmail());
        chkAdmin.setSelected(u.getIs_admin());
        txtPassword.setText(u.getPassword());
    }

    @FXML
    public void onAdd() {
        lblError.setText("");
        if (!validateRequired()) return;
        User u = new User();
        u.setEmail(txtEmail.getText().trim());
        u.setPassword(txtPassword.getText());
        u.setIs_admin(chkAdmin.isSelected());
        userService.create(u);
        clearForm();
        refresh();
    }

    @FXML
    public void onEdit() {
        lblError.setText("");
        User sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (!validateRequired()) return;
        sel.setEmail(txtEmail.getText().trim());
        sel.setPassword(txtPassword.getText());
        sel.setIs_admin(chkAdmin.isSelected());
        userService.update(sel);
        refresh();
    }

    @FXML
    public void onDelete() {
        User sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        userService.delete(sel);
        refresh();
        clearForm();
    }

    @FXML
    public void onCerrar() {
        Stage stage = (Stage) tablaUsuarios.getScene().getWindow();
        stage.close();
    }

    private boolean validateRequired() {
        if (txtEmail.getText().trim().isEmpty() || txtPassword.getText().isEmpty()) {
            lblError.setText("Email y contraseña requeridos.");
            return false;
        }
        if (!txtEmail.getText().contains("@")) {
            lblError.setText("Email inválido.");
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtEmail.clear();
        txtPassword.clear();
        chkAdmin.setSelected(false);
        tablaUsuarios.getSelectionModel().clearSelection();
    }
}

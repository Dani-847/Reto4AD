package org.drk.reto2diad.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.drk.reto2diad.copia.Copia;
import org.drk.reto2diad.copia.CopiaService;
import org.drk.reto2diad.pelicula.Pelicula;
import org.drk.reto2diad.pelicula.PeliculaService;
import org.drk.reto2diad.session.SimpleSessionService;
import org.drk.reto2diad.user.User;
import org.drk.reto2diad.utils.JavaFXUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private TableView<Pelicula> tablaPeliculas;
    @FXML private TableColumn<Pelicula,String> colPeliId;
    @FXML private TableColumn<Pelicula,String> colPeliTitulo;
    @FXML private TableColumn<Pelicula,String> colPeliAnio;
    @FXML private TableColumn<Pelicula,String> colPeliGenero;
    @FXML private TableColumn<Pelicula,String> colPeliDirector;

    @FXML private TableView<Copia> tablaCopias;
    @FXML private TableColumn<Copia,String> colCopiaId;
    @FXML private TableColumn<Copia,String> colCopiaTitulo;
    @FXML private TableColumn<Copia,String> colCopiaEstado;
    @FXML private TableColumn<Copia,String> colCopiaSoporte;

    @FXML private Label lblUsuarioActual;
    @FXML private HBox boxAdmin;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbSoporte;
    @FXML private Button btnGestionUsuarios;

    @FXML private Label lblWinTitle;

    private final SimpleSessionService sessionService = new SimpleSessionService();
    private final PeliculaService peliculaService = new PeliculaService();
    private final CopiaService copiaService = new CopiaService();
    private User active;

    private double xOffset, yOffset;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        active = sessionService.getActive();
        setupTables();
        refreshData();
        lblUsuarioActual.setText(formatCorreoAdmin(active));

        boolean admin = sessionService.isAdmin();
        boxAdmin.setVisible(admin);
        boxAdmin.setManaged(admin);
        btnGestionUsuarios.setVisible(admin);
        btnGestionUsuarios.setManaged(admin);

        String winTitle = "Gestor de copias y películas" + (admin ? " (admin)" : "");
        if (JavaFXUtil.getStage() != null) JavaFXUtil.getStage().setTitle(winTitle);
        if (lblWinTitle != null) lblWinTitle.setText(winTitle);

        cmbEstado.setItems(FXCollections.observableArrayList("bueno","nuevo","regular","dañado"));
        cmbEstado.getSelectionModel().selectFirst();
        cmbSoporte.setItems(FXCollections.observableArrayList("DVD","BluRay","Digital","VHS"));
        cmbSoporte.getSelectionModel().selectFirst();

        tablaPeliculas.getSelectionModel().selectedItemProperty().addListener(showPelicula());
    }

    private void setupTables() {
        colPeliId.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getId())));
        colPeliTitulo.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getTitulo()));
        colPeliAnio.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getAnio() != null ? r.getValue().getAnio().toString() : "-"));
        colPeliGenero.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getGenero()));
        colPeliDirector.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getDirector()));

        colCopiaId.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getId())));
        colCopiaTitulo.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getMovie() != null ? r.getValue().getMovie().getTitulo() : "-"));
        colCopiaEstado.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getEstado()));
        colCopiaSoporte.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getSoporte()));

        tablaPeliculas.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tablaCopias.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void refreshData() {
        tablaPeliculas.getItems().clear();
        tablaCopias.getItems().clear();
        setupTables();
        tablaPeliculas.setItems(FXCollections.observableArrayList(peliculaService.findAll()));
        tablaCopias.setItems(FXCollections.observableArrayList(copiaService.findByUser(active)));
        tablaCopias.getSelectionModel().clearSelection();
        tablaPeliculas.getSelectionModel().clearSelection();
    }

    private String formatCorreoAdmin(User u) {
        String base = u.getEmail();
        return sessionService.isAdmin() ? base + " (Vista de administrador)" : base;
    }

    private ChangeListener<Pelicula> showPelicula() {
        return (obs, oldSel, newSel) -> {
            if (newSel != null) {
                JavaFXUtil.showModal(
                        Alert.AlertType.INFORMATION,
                        newSel.getTitulo(),
                        newSel.getTitulo(),
                        newSel.toString()
                );
            }
        };
    }

    @FXML
    public void onCreateCopia(ActionEvent e) {
        Pelicula selected = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            JavaFXUtil.showModal(Alert.AlertType.WARNING, "Copia", "Película no seleccionada", "Selecciona una película primero.");
            return;
        }
        copiaService.create(active, selected, cmbEstado.getValue(), cmbSoporte.getValue());
        refreshData();
    }

    @FXML
    public void onEditCopia(ActionEvent e) {
        Copia copia = tablaCopias.getSelectionModel().getSelectedItem();
        if (copia == null) return;
        var updated = copiaService.update(Long.valueOf(copia.getId()), active, cmbEstado.getValue(), cmbSoporte.getValue());
        if (updated.isEmpty()) {
            JavaFXUtil.showModal(Alert.AlertType.ERROR, "Editar", "No permitido", "Solo tus copias.");
            return;
        }
        tablaCopias.refresh();
        refreshData();
    }

    @FXML
    public void onDeleteCopia(ActionEvent e) {
        Copia copia = tablaCopias.getSelectionModel().getSelectedItem();
        if (copia == null) return;
        var deleted = copiaService.delete(Long.valueOf(copia.getId()), active);
        if (deleted.isEmpty()) {
            JavaFXUtil.showModal(Alert.AlertType.ERROR, "Eliminar", "No permitido", "Solo tus copias.");
            return;
        }
        tablaCopias.getSelectionModel().clearSelection();
        tablaCopias.refresh();
        refreshData();
    }

    @FXML
    public void onAddPelicula(ActionEvent e) {
        if (!sessionService.isAdmin()) return;
        openPeliculaDialog(null);
    }

    @FXML
    public void onEditPelicula(ActionEvent e) {
        if (!sessionService.isAdmin()) return;
        Pelicula sel = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        openPeliculaDialog(sel);
    }

    private void openPeliculaDialog(Pelicula pelicula) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/drk/reto2diad/pelicula-dialog.fxml"));
            Parent root = loader.load();
            PeliculaDialogController ctrl = loader.getController();
            ctrl.init(pelicula);

            Stage dialog = new Stage();
            dialog.initOwner(JavaFXUtil.getStage());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(pelicula == null ? "Añadir película" : "Editar película");
            dialog.setScene(new Scene(root));
            dialog.setOnShown(ev -> {
                Stage owner = JavaFXUtil.getStage();
                if (owner != null) {
                    double x = owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2;
                    double y = owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2;
                    dialog.setX(x);
                    dialog.setY(y);
                } else {
                    dialog.centerOnScreen();
                }
            });
            dialog.showAndWait();

            Pelicula result = ctrl.getResult();
            if (result != null) {
                if (result.getId() == null) peliculaService.create(result);
                else peliculaService.update(result);
                refreshData();
            }
        } catch (Exception ex) {
            JavaFXUtil.showModal(Alert.AlertType.ERROR, "Error", "Diálogo película", ex.getMessage());
        }
    }

    @FXML
    public void onDeletePelicula(ActionEvent e) {
        if (!sessionService.isAdmin()) return;
        Pelicula sel = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        peliculaService.delete(sel);
        refreshData();
    }

    @FXML
    public void onGestionUsuarios(ActionEvent e) {
        if (!sessionService.isAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/drk/reto2diad/users-dialog.fxml"));
            Parent root = loader.load();
            UsersDialogController ctrl = loader.getController();
            if (ctrl != null) ctrl.init();

            Stage dialog = new Stage();
            dialog.initOwner(JavaFXUtil.getStage());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Gestión de usuarios");
            dialog.setScene(new Scene(root));
            dialog.setOnShown(ev -> {
                Stage owner = JavaFXUtil.getStage();
                if (owner != null) {
                    double x = owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2;
                    double y = owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2;
                    dialog.setX(x);
                    dialog.setY(y);
                } else {
                    dialog.centerOnScreen();
                }
            });
            dialog.showAndWait();
        } catch (Exception ex) {
            JavaFXUtil.showModal(Alert.AlertType.ERROR, "Error", "Gestión usuarios", ex.getMessage());
        }
    }

    @FXML
    public void onSalir(ActionEvent actionEvent) {
        sessionService.logout();
        JavaFXUtil.setScene("/org/drk/reto2diad/login-view.fxml");
    }

    @FXML
    private void onTitleBarPressed(MouseEvent e) {
        Stage stage = JavaFXUtil.getStage();
        if (stage == null) stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        xOffset = stage.getX() - e.getScreenX();
        yOffset = stage.getY() - e.getScreenY();
    }

    @FXML
    private void onTitleBarDragged(MouseEvent e) {
        Stage stage = JavaFXUtil.getStage();
        if (stage == null) stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setX(e.getScreenX() + xOffset);
        stage.setY(e.getScreenY() + yOffset);
    }

    @FXML
    private void onMinimizeWindow(ActionEvent e) {
        Stage stage = JavaFXUtil.getStage();
        if (stage == null) stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void onCloseWindow(ActionEvent e) {
        Stage stage = JavaFXUtil.getStage();
        if (stage == null) stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
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

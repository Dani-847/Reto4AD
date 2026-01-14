package org.drk.reto2diad.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

    @FXML private TextField txtBuscarTitulo;

    @FXML private Button btnUndoPelicula;

    private final SimpleSessionService sessionService = new SimpleSessionService();
    private final PeliculaService peliculaService = new PeliculaService();
    private final CopiaService copiaService = new CopiaService();
    private User active;

    private double xOffset, yOffset;

    private final ObservableList<Pelicula> peliculasMaster = FXCollections.observableArrayList();
    private FilteredList<Pelicula> peliculasFiltradas;

    // --------- Infraestructura de UNDO ---------

    private enum MovieOpType { CREATE, UPDATE, DELETE }

    private static final class MovieUndoAction {
        final MovieOpType type;
        final Pelicula before; // estado previo (para UPDATE/DELETE). En CREATE puede ser null.
        final Pelicula after;  // estado posterior (para CREATE/UPDATE). En DELETE puede ser null.

        MovieUndoAction(MovieOpType type, Pelicula before, Pelicula after) {
            this.type = type;
            this.before = before;
            this.after = after;
        }
    }

    private MovieUndoAction lastMovieUndo = null;

    private static Pelicula snapshot(Pelicula p) {
        if (p == null) return null;
        Pelicula c = new Pelicula();
        c.setId(p.getId());
        c.setTitulo(p.getTitulo());
        c.setAnio(p.getAnio());
        c.setGenero(p.getGenero());
        c.setDirector(p.getDirector());
        return c;
    }

    private void setUndo(MovieUndoAction action) {
        lastMovieUndo = action;
        if (btnUndoPelicula != null) {
            btnUndoPelicula.setDisable(action == null);
            btnUndoPelicula.setManaged(true);
            btnUndoPelicula.setVisible(true);
        }
    }

    private void clearUndo() {
        lastMovieUndo = null;
        if (btnUndoPelicula != null) btnUndoPelicula.setDisable(true);
    }

    // ---------------------------------------------------------------

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        active = sessionService.getActive();
        setupTables();
        setupBusquedaTitulo();
        setupDetallePeliculaDobleClick();
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

        // estado inicial de undo
        if (btnUndoPelicula != null) btnUndoPelicula.setDisable(true);
    }

    private void setupDetallePeliculaDobleClick() {
        tablaPeliculas.setRowFactory(tv -> {
            TableRow<Pelicula> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    Pelicula p = row.getItem();
                    JavaFXUtil.showModal(Alert.AlertType.INFORMATION, p.getTitulo(), p.getTitulo(), p.toString());
                }
            });
            return row;
        });
    }

    private void setupBusquedaTitulo() {
        peliculasFiltradas = new FilteredList<>(peliculasMaster, p -> true);

        SortedList<Pelicula> peliculasOrdenadas = new SortedList<>(peliculasFiltradas);
        peliculasOrdenadas.comparatorProperty().bind(tablaPeliculas.comparatorProperty());

        tablaPeliculas.setItems(peliculasOrdenadas);

        if (txtBuscarTitulo != null) {
            txtBuscarTitulo.textProperty().addListener((obs, oldV, newV) -> {
                final String q = (newV == null) ? "" : newV.trim().toLowerCase();
                peliculasFiltradas.setPredicate(p -> {
                    if (q.isEmpty()) return true;
                    String t = p.getTitulo();
                    return t != null && t.toLowerCase().contains(q);
                });
            });
        }
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
        peliculasMaster.setAll(peliculaService.findAll());
        tablaCopias.setItems(FXCollections.observableArrayList(copiaService.findByUser(active)));
    }

    @FXML
    public void onClearBuscarTitulo(ActionEvent e) {
        if (txtBuscarTitulo != null) txtBuscarTitulo.clear();
    }

    private String formatCorreoAdmin(User u) {
        String base = u.getEmail();
        return sessionService.isAdmin() ? base + " (Vista de administrador)" : base;
    }

    // ----------------- Handler undo -----------------

    @FXML
    public void onUndoPelicula(ActionEvent e) {
        if (!sessionService.isAdmin()) return;
        if (lastMovieUndo == null) return;

        try {
            switch (lastMovieUndo.type) {
                case CREATE -> {
                    // deshacer CREATE = borrar lo creado
                    Pelicula created = lastMovieUndo.after;
                    if (created != null) {
                        peliculaService.delete(created);
                    }
                }
                case UPDATE -> {
                    // deshacer UPDATE = restaurar "before"
                    Pelicula before = lastMovieUndo.before;
                    if (before != null) {
                        peliculaService.update(before);
                    }
                }
                case DELETE -> {
                    // deshacer DELETE = re-crear la película eliminada (mismo id si Hibernate lo permite con merge)
                    Pelicula deleted = lastMovieUndo.before;
                    if (deleted != null) {
                        peliculaService.update(deleted);
                    }
                }
            }

            clearUndo();
            refreshData();
        } catch (Exception ex) {
            JavaFXUtil.showModal(Alert.AlertType.ERROR, "Error", "Deshacer película", ex.getMessage());
        }
    }

    // -------------------------------------------------------

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
    }

    @FXML
    public void onAddPelicula(ActionEvent e) {
        if (!sessionService.isAdmin()) return;

        // NO hay "before" aquí; se captura al cerrar el diálogo si realmente se creó
        openPeliculaDialog(null);
    }

    @FXML
    public void onEditPelicula(ActionEvent e) {
        if (!sessionService.isAdmin()) return;

        Pelicula sel = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        // Snapshot antes de editar
        Pelicula before = snapshot(sel);
        openPeliculaDialog(sel);

        // Si el diálogo guardó cambios, la lista se refresca dentro; se intenta detectar el "after"
        // (cargamos por id)
        try {
            Pelicula after = peliculaService.findById(Long.valueOf(sel.getId())).map(MainController::snapshot).orElse(null);
            if (after != null && before != null) {
                boolean changed =
                        (before.getTitulo() != null ? !before.getTitulo().equals(after.getTitulo()) : after.getTitulo() != null) ||
                                (before.getGenero() != null ? !before.getGenero().equals(after.getGenero()) : after.getGenero() != null) ||
                                (before.getDirector() != null ? !before.getDirector().equals(after.getDirector()) : after.getDirector() != null) ||
                                (before.getAnio() != null ? !before.getAnio().equals(after.getAnio()) : after.getAnio() != null);

                if (changed) setUndo(new MovieUndoAction(MovieOpType.UPDATE, before, after));
            }
        } catch (Exception ignore) {
            // si falla la detección de cambios no se arma undo
        }
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
                boolean isCreate = (result.getId() == null);

                Pelicula saved = isCreate ? peliculaService.create(result) : peliculaService.update(result);
                refreshData();

                if (isCreate) {
                    // deshacer CREATE => borrar lo creado
                    setUndo(new MovieUndoAction(MovieOpType.CREATE, null, snapshot(saved)));
                }
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

        Pelicula before = snapshot(sel);
        peliculaService.delete(sel);

        // deshacer DELETE => volver a guardar el estado anterior
        setUndo(new MovieUndoAction(MovieOpType.DELETE, before, null));

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

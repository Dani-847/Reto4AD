package org.drk.reto2diad.controllers;

import javafx.animation.PauseTransition;
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
import javafx.util.Duration;
import org.drk.reto2diad.copia.Copia;
import org.drk.reto2diad.copia.CopiaService;
import org.drk.reto2diad.pelicula.Pelicula;
import org.drk.reto2diad.pelicula.PeliculaService;
import org.drk.reto2diad.session.SimpleSessionService;
import org.drk.reto2diad.user.User;
import org.drk.reto2diad.utils.JavaFXUtil;

import java.net.URL;
import java.util.List;
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

    @FXML private Label lblLogger;

    // --------- Logger UI (feedback no modal) ---------

    private enum UiLogType { OK, WARN, ERROR }

    private PauseTransition loggerAutoClear;

    private void log(UiLogType type, String msg) {
        if (lblLogger == null) return;

        lblLogger.setText(msg == null ? "" : msg);

        // estilo mínimo sin depender de CSS externo
        String base = "-fx-padding: 6 10 6 10; -fx-background-radius: 6; -fx-font-size: 12;";
        String style = switch (type) {
            case OK -> base + " -fx-text-fill: #0f5132; -fx-background-color: #d1e7dd;";
            case WARN -> base + " -fx-text-fill: #664d03; -fx-background-color: #fff3cd;";
            case ERROR -> base + " -fx-text-fill: #842029; -fx-background-color: #f8d7da;";
        };
        lblLogger.setStyle(style);
        lblLogger.setVisible(true);
        lblLogger.setManaged(true);

        if (loggerAutoClear != null) loggerAutoClear.stop();
        loggerAutoClear = new PauseTransition(Duration.seconds(type == UiLogType.ERROR ? 8 : 4));
        loggerAutoClear.setOnFinished(ev -> clearLog());
        loggerAutoClear.playFromStart();
    }

    private void logOk(String msg) { log(UiLogType.OK, msg); }
    private void logWarn(String msg) { log(UiLogType.WARN, msg); }
    private void logError(String msg) { log(UiLogType.ERROR, msg); }

    private void clearLog() {
        if (lblLogger == null) return;
        lblLogger.setText("");
        lblLogger.setVisible(false);
        lblLogger.setManaged(false);
    }

    private static String safeMsg(Exception ex) {
        if (ex == null) return "Error desconocido.";
        String m = ex.getMessage();
        return (m == null || m.isBlank()) ? ex.getClass().getSimpleName() : m;
    }

    // --------- Infraestructura de UNDO ---------

    private enum MovieOpType { CREATE, UPDATE, DELETE }

    private static final class MovieUndoAction {
        final MovieOpType type;
        final Pelicula before;
        final Pelicula after;

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

        if (btnUndoPelicula != null) btnUndoPelicula.setDisable(true);

        if (txtBuscarTitulo != null) txtBuscarTitulo.setTooltip(new Tooltip("Filtra la tabla por título (contiene)."));
        if (cmbEstado != null) cmbEstado.setTooltip(new Tooltip("Estado físico de la copia."));
        if (cmbSoporte != null) cmbSoporte.setTooltip(new Tooltip("Formato de la copia (DVD, BluRay, etc.)."));
        if (btnUndoPelicula != null) btnUndoPelicula.setTooltip(new Tooltip("Deshace la última operación sobre películas (solo admin)."));

        clearLog();
        logOk("Listo.");
    }

    private void setupDetallePeliculaDobleClick() {
        tablaPeliculas.setRowFactory(tv -> {
            TableRow<Pelicula> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    Pelicula p = row.getItem();
                    JavaFXUtil.showModal(Alert.AlertType.INFORMATION, p.getTitulo(), p.getTitulo(), p.toString());
                    logOk("Mostrando detalles de: " + (p.getTitulo() != null ? p.getTitulo() : "(sin título)"));
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
                if (q.isEmpty()) logOk("Filtro limpiado.");
                else logOk("Filtrando por: " + q);
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
        try {
            peliculasMaster.setAll(peliculaService.findAll());
            tablaCopias.setItems(FXCollections.observableArrayList(copiaService.findByUser(active)));
        } catch (Exception ex) {
            logError("No se pudo refrescar datos: " + safeMsg(ex));
        }
    }

    @FXML
    public void onClearBuscarTitulo(ActionEvent e) {
        if (txtBuscarTitulo != null) txtBuscarTitulo.clear();
        logOk("Búsqueda limpiada.");
    }

    private String formatCorreoAdmin(User u) {
        String base = u.getEmail();
        return sessionService.isAdmin() ? base + " (Vista de administrador)" : base;
    }

    // ----------------- Handler undo -----------------

    @FXML
    public void onUndoPelicula(ActionEvent e) {
        if (!sessionService.isAdmin()) {
            logWarn("Acción no permitida: solo admin.");
            return;
        }
        if (lastMovieUndo == null) {
            logWarn("No hay acción para deshacer.");
            return;
        }

        try {
            switch (lastMovieUndo.type) {
                case CREATE -> {
                    Pelicula created = lastMovieUndo.after;
                    if (created != null) peliculaService.delete(created);
                    logOk("Deshecho: creación de película.");
                }
                case UPDATE -> {
                    Pelicula before = lastMovieUndo.before;
                    if (before != null) peliculaService.update(before);
                    logOk("Deshecho: edición de película.");
                }
                case DELETE -> {
                    Pelicula deleted = lastMovieUndo.before;
                    if (deleted != null) {
                        Pelicula toCreate = snapshot(deleted);
                        toCreate.setId(null);
                        peliculaService.create(toCreate);
                        logOk("Deshecho: eliminación de película.");
                    }
                }
            }

            clearUndo();
            refreshData();
        } catch (Exception ex) {
            logError("Error al deshacer: " + safeMsg(ex));
        }
    }

    // -------------------------------------------------------

    @FXML
    public void onCreateCopia(ActionEvent e) {
        Pelicula selected = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (selected == null) {
            logWarn("Selecciona una película antes de crear una copia.");
            return;
        }
        try {
            copiaService.create(active, selected, cmbEstado.getValue(), cmbSoporte.getValue());
            refreshData();
            logOk("Copia creada.");
        } catch (Exception ex) {
            logError("No se pudo crear la copia: " + safeMsg(ex));
        }
    }

    @FXML
    public void onEditCopia(ActionEvent e) {
        Copia copia = tablaCopias.getSelectionModel().getSelectedItem();
        if (copia == null) {
            logWarn("Selecciona una copia para editar.");
            return;
        }

        Integer copiaId = copia.getId();
        if (copiaId == null) {
            logError("La copia seleccionada no tiene ID.");
            return;
        }

        try {
            var updated = copiaService.update(copiaId, active, cmbEstado.getValue(), cmbSoporte.getValue());
            if (updated.isEmpty()) {
                logWarn("No permitido: solo puedes editar tus copias.");
                return;
            }
            refreshData();
            logOk("Copia actualizada.");
        } catch (Exception ex) {
            logError("No se pudo editar la copia: " + safeMsg(ex));
        }
    }

    @FXML
    public void onDeleteCopia(ActionEvent e) {
        Copia copia = tablaCopias.getSelectionModel().getSelectedItem();
        if (copia == null) {
            logWarn("Selecciona una copia para eliminar.");
            return;
        }

        Integer copiaId = copia.getId();
        if (copiaId == null) {
            logError("La copia seleccionada no tiene ID.");
            return;
        }

        try {
            var deleted = copiaService.delete(copiaId, active);
            if (deleted.isEmpty()) {
                logWarn("No permitido: solo puedes eliminar tus copias.");
                return;
            }
            refreshData();
            logOk("Copia eliminada.");
        } catch (Exception ex) {
            logError("No se pudo eliminar la copia: " + safeMsg(ex));
        }
    }

    @FXML
    public void onAddPelicula(ActionEvent e) {
        if (!sessionService.isAdmin()) {
            logWarn("Acción no permitida: solo admin.");
            return;
        }
        openPeliculaDialog(null);
    }

    @FXML
    public void onEditPelicula(ActionEvent e) {
        if (!sessionService.isAdmin()) {
            logWarn("Acción no permitida: solo admin.");
            return;
        }

        Pelicula sel = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            logWarn("Selecciona una película para editar.");
            return;
        }

        Pelicula before = snapshot(sel);
        openPeliculaDialog(sel);

        try {
            Pelicula after = peliculaService.findById(Long.valueOf(sel.getId())).map(MainController::snapshot).orElse(null);
            if (after != null && before != null) {
                boolean changed =
                        (before.getTitulo() != null ? !before.getTitulo().equals(after.getTitulo()) : after.getTitulo() != null) ||
                                (before.getGenero() != null ? !before.getGenero().equals(after.getGenero()) : after.getGenero() != null) ||
                                (before.getDirector() != null ? !before.getDirector().equals(after.getDirector()) : after.getDirector() != null) ||
                                (before.getAnio() != null ? !before.getAnio().equals(after.getAnio()) : after.getAnio() != null);

                if (changed) {
                    setUndo(new MovieUndoAction(MovieOpType.UPDATE, before, after));
                    logOk("Película actualizada.");
                } else {
                    logOk("Edición cancelada o sin cambios.");
                }
            }
        } catch (Exception ex) {
            logWarn("No se pudo verificar cambios: " + safeMsg(ex));
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

            while (true) {
                dialog.showAndWait();

                Pelicula result = ctrl.getResult();
                if (result == null) {
                    logOk("Acción cancelada.");
                    return;
                }

                try {
                    boolean isCreate = (result.getId() == null);
                    Pelicula saved = isCreate ? peliculaService.create(result) : peliculaService.update(result);

                    refreshData();

                    if (isCreate) {
                        setUndo(new MovieUndoAction(MovieOpType.CREATE, null, snapshot(saved)));
                        logOk("Película creada.");
                    }

                    return;
                } catch (Exception ex) {
                    ctrl.showDbError(ex);
                    logError("Error al guardar: " + safeMsg(ex));
                    dialog.show();
                    dialog.hide();
                }
            }
        } catch (Exception ex) {
            logError("Error abriendo diálogo: " + safeMsg(ex));
        }
    }

    @FXML
    public void onDeletePelicula(ActionEvent e) {
        if (!sessionService.isAdmin()) {
            logWarn("Acción no permitida: solo admin.");
            return;
        }

        Pelicula sel = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            logWarn("Selecciona una película para eliminar.");
            return;
        }

        try {
            List<Copia> copias = copiaService.findByMovie(sel);
            if (copias != null && !copias.isEmpty()) {
                logWarn("No se puede eliminar: tiene " + copias.size() + " copia(s) asociada(s).");
                return;
            }
        } catch (Exception ex) {
            logError("Error comprobando copias: " + safeMsg(ex));
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar la película seleccionada?");
        String titulo = (sel.getTitulo() != null && !sel.getTitulo().isBlank()) ? sel.getTitulo() : "(sin título)";
        confirm.setContentText("Se eliminará: " + titulo);

        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnEliminar, btnCancelar);

        var res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != btnEliminar) {
            logOk("Eliminación cancelada.");
            return;
        }

        try {
            Pelicula before = snapshot(sel);
            peliculaService.delete(sel);

            setUndo(new MovieUndoAction(MovieOpType.DELETE, before, null));
            refreshData();
            logOk("Película eliminada.");
        } catch (Exception ex) {
            logError("No se pudo eliminar la película: " + safeMsg(ex));
        }
    }

    @FXML
    public void onGestionUsuarios(ActionEvent e) {
        if (!sessionService.isAdmin()) {
            logWarn("Acción no permitida: solo admin.");
            return;
        }
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
            logOk("Gestión de usuarios cerrada.");
        } catch (Exception ex) {
            logError("Error abriendo gestión de usuarios: " + safeMsg(ex));
        }
    }

    @FXML
    public void onAyuda(ActionEvent e) {
        String help = """
            Objetivo:
            Gestionar películas y tus copias asociadas.

            Películas (izquierda):
            - Buscar por título con el campo “Buscar título”.
            - Doble clic en una película para ver detalles.
            - Admin: puede añadir/editar/eliminar películas y deshacer la última acción.

            Copias (derecha):
            - Selecciona una película y elige Estado/Soporte para crear una copia.
            - Puedes editar/eliminar solo tus copias.

            Login:
            - Si el correo no existe o la contraseña es incorrecta, se indica en rojo.
            """;
        JavaFXUtil.showModal(Alert.AlertType.INFORMATION, "Ayuda", "Guía rápida", help);
        logOk("Ayuda mostrada.");
    }

    @FXML
    public void onSalir(ActionEvent actionEvent) {
        sessionService.logout();
        logOk("Sesión cerrada.");
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
        logOk("Ventana minimizada.");
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

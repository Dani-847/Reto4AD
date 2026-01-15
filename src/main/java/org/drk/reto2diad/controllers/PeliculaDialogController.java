package org.drk.reto2diad.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.drk.reto2diad.pelicula.Pelicula;

import java.time.Year;
import java.util.List;

public class PeliculaDialogController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtAnio;
    @FXML private ComboBox<String> cmbGenero;
    @FXML private TextField txtDirector;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;

    private Pelicula editing;
    private Pelicula result;

    @FXML
    private void initialize() {
        if (txtTitulo != null) txtTitulo.setPromptText("Ej: Casablanca");
        if (txtAnio != null) txtAnio.setPromptText("Ej: 1999 (mín 1888)");
        if (txtDirector != null) txtDirector.setPromptText("Ej: Christopher Nolan");

        if (cmbGenero != null) {
            List<String> generos = List.of(
                    "Acción", "Aventura", "Animación", "Ciencia ficción", "Comedia", "Crimen",
                    "Documental", "Drama", "Fantasía", "Historia", "Misterio", "Musical",
                    "Romance", "Suspense", "Terror", "Western"
            );
            cmbGenero.setItems(FXCollections.observableArrayList(generos));
            cmbGenero.setEditable(true);
            cmbGenero.setVisibleRowCount(10);
            cmbGenero.setPromptText("Ej: Drama");
        }
    }

    public void init(Pelicula pelicula) {
        this.editing = pelicula;

        if (pelicula != null) {
            if (txtTitulo != null) txtTitulo.setText(pelicula.getTitulo());
            if (txtAnio != null) txtAnio.setText(pelicula.getAnio() != null ? String.valueOf(pelicula.getAnio()) : "");
            if (cmbGenero != null) cmbGenero.setValue(pelicula.getGenero());
            if (txtDirector != null) txtDirector.setText(pelicula.getDirector());
        }
    }

    public Pelicula getResult() {
        return result;
    }

    public void showDbError(Throwable ex) {
        String msg = ex != null ? ex.getMessage() : null;
        if (msg == null || msg.isBlank()) msg = "Error al guardar.";
        if (msg.contains("Column 'año' cannot be null") || msg.contains("año") && msg.contains("cannot be null")) {
            msg = "El año es obligatorio.";
        }
        showError(msg);
    }

    @FXML
    private void onGuardar() {
        if (lblError != null) lblError.setText("");

        final String titulo = txtTitulo != null ? safeTrim(txtTitulo.getText()) : null;
        final String anioStr = txtAnio != null ? safeTrim(txtAnio.getText()) : null;
        final String genero = cmbGenero != null ? safeTrim(cmbGenero.getEditor().getText()) : null;
        final String director = txtDirector != null ? safeTrim(txtDirector.getText()) : null;

        if (titulo == null || titulo.isBlank()) {
            showError("El título es obligatorio.");
            return;
        }

        if (genero == null || genero.isBlank()) {
            showError("El género es obligatorio.");
            return;
        }

        // IMPORTANTE: si tu columna `año` es NOT NULL, aquí debe ser obligatorio
        if (anioStr == null || anioStr.isBlank()) {
            showError("El año es obligatorio.");
            return;
        }

        Integer anio;
        try {
            anio = Integer.parseInt(anioStr);
        } catch (NumberFormatException ex) {
            showError("El año debe ser numérico (ej: 1999).");
            return;
        }

        final int minYear = 1888;
        final int maxYear = Year.now().getValue() + 1;
        if (anio < minYear || anio > maxYear) {
            showError("El año debe estar entre " + minYear + " y " + maxYear + ".");
            return;
        }

        if (editing == null) editing = new Pelicula();
        editing.setTitulo(titulo);
        editing.setGenero(genero);
        editing.setAnio(anio);
        editing.setDirector((director != null && !director.isBlank()) ? director : null);

        result = editing;
        btnGuardar.getScene().getWindow().hide();
    }

    @FXML
    private void onCancelar() {
        result = null;
        btnGuardar.getScene().getWindow().hide();
    }

    private void showError(String msg) {
        if (lblError != null) lblError.setText(msg);
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}

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
        // Placeholders / ayuda
        if (txtTitulo != null) txtTitulo.setPromptText("Ej: Casablanca");
        if (txtAnio != null) txtAnio.setPromptText("Ej: 1999 (mín 1888)");
        if (txtDirector != null) txtDirector.setPromptText("Ej: Christopher Nolan");

        // Sugerencias para género
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

    @FXML
    private void onGuardar() {
        if (lblError != null) lblError.setText("");

        final String titulo = txtTitulo != null ? safeTrim(txtTitulo.getText()) : null;
        final String anioStr = txtAnio != null ? safeTrim(txtAnio.getText()) : null;
        final String genero = cmbGenero != null ? safeTrim(cmbGenero.getEditor().getText()) : null;
        final String director = txtDirector != null ? safeTrim(txtDirector.getText()) : null;

        // 1) Validación: título obligatorio
        if (titulo == null || titulo.isBlank()) {
            showError("El título es obligatorio.");
            return;
        }

        // 2) Validación: género obligatorio (evita: not-null property ... genero)
        if (genero == null || genero.isBlank()) {
            showError("El género es obligatorio.");
            return;
        }

        // 3) Validación: año opcional, pero si viene debe ser número y realista
        Integer anio = null;
        if (anioStr != null && !anioStr.isBlank()) {
            try {
                anio = Integer.parseInt(anioStr);
            } catch (NumberFormatException ex) {
                showError("El año debe ser numérico (ej: 1999).");
                return;
            }

            final int minYear = 1888;
            final int maxYear = Year.now().getValue() + 1; // margen para próximos estrenos
            if (anio < minYear || anio > maxYear) {
                showError("El año debe estar entre " + minYear + " y " + maxYear + ".");
                return;
            }
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

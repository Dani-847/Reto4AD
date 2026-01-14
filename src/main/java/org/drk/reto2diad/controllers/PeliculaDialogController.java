package org.drk.reto2diad.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.drk.reto2diad.pelicula.Pelicula;

/**
 * Diálogo de Película (sin acceso directo a repos).
 * Devuelve entidad preparada para que el MainController la persista vía servicio.
 */
public class PeliculaDialogController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtAnio;
    @FXML private TextField txtGenero;
    @FXML private TextField txtDirector;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;

    private Pelicula editing;
    private Pelicula result;

    public void init(Pelicula pelicula) {
        this.editing = pelicula;
        if (pelicula != null) {
            txtTitulo.setText(pelicula.getTitulo());
            txtAnio.setText(pelicula.getAnio() != null ? String.valueOf(pelicula.getAnio()) : "");
            txtGenero.setText(pelicula.getGenero());
            txtDirector.setText(pelicula.getDirector());
        }
    }

    public Pelicula getResult() {
        return result;
    }

    @FXML
    private void onGuardar() {
        lblError.setText("");
        String titulo = txtTitulo.getText();
        String anioStr = txtAnio.getText();
        String genero = txtGenero.getText();
        String director = txtDirector.getText();

        if (titulo == null || titulo.isBlank()) {
            lblError.setText("El título es obligatorio.");
            return;
        }

        Integer anio = null;
        if (anioStr != null && !anioStr.isBlank()) {
            try {
                anio = Integer.parseInt(anioStr);
            } catch (NumberFormatException ex) {
                lblError.setText("El año debe ser numérico.");
                return;
            }
        }

        if (editing == null) editing = new Pelicula();
        editing.setTitulo(titulo);
        editing.setAnio(anio);
        editing.setGenero(genero);
        editing.setDirector(director);

        result = editing;
        btnGuardar.getScene().getWindow().hide();
    }

    @FXML
    private void onCancelar() {
        result = null;
        btnGuardar.getScene().getWindow().hide();
    }
}

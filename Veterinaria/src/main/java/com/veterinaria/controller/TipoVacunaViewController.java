package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.TipoVacuna;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

/**
 * Vista del módulo de tipos de vacuna. Solo se ocupa de la interfaz y
 * delega las reglas de negocio a TipoVacunaController.
 */
public class TipoVacunaViewController {

    @FXML private TextField txtNombreComercial;
    @FXML private TextField txtEnfermedad;
    @FXML private TextField txtPeriodicidad;
    @FXML private CheckBox chkCiclica;

    @FXML private TableView<TipoVacuna> tablaTipoVacunas;
    @FXML private TableColumn<TipoVacuna, String> colNombreComercial;
    @FXML private TableColumn<TipoVacuna, String> colEnfermedad;
    @FXML private TableColumn<TipoVacuna, Integer> colPeriodicidad;
    @FXML private TableColumn<TipoVacuna, String> colCiclica;

    private TipoVacunaController tipoVacunaController;
    private TipoVacuna tipoEnEdicion = null;

    @FXML
    public void initialize() {
        tipoVacunaController = new TipoVacunaController(JpaUtil.getEntityManager());

        txtPeriodicidad.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtPeriodicidad.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        colNombreComercial.setCellValueFactory(new PropertyValueFactory<>("nombreComercial"));
        colEnfermedad.setCellValueFactory(new PropertyValueFactory<>("enfermedadQuePreviene"));
        colPeriodicidad.setCellValueFactory(new PropertyValueFactory<>("periodicidadMeses"));
        colCiclica.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().isEsCiclica() ? "Sí" : "No"
                )
        );

        tablaTipoVacunas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        cargarTiposVacuna();

        tablaTipoVacunas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, tipo) -> {
            if (tipo != null) {
                this.tipoEnEdicion = tipo;
                txtNombreComercial.setText(tipo.getNombreComercial());
                txtEnfermedad.setText(tipo.getEnfermedadQuePreviene());
                txtPeriodicidad.setText(String.valueOf(tipo.getPeriodicidadMeses()));
                chkCiclica.setSelected(tipo.isEsCiclica());
            }
        });
    }

    private void cargarTiposVacuna() {
        tablaTipoVacunas.setItems(
                FXCollections.observableArrayList(tipoVacunaController.listarTodos())
        );
    }

    @FXML
    public void guardarTipoVacuna() {
        if (!validarCampos()) {
            return;
        }

        try {
            TipoVacuna nuevo = new TipoVacuna(
                    txtNombreComercial.getText().trim(),
                    txtEnfermedad.getText().trim(),
                    Integer.parseInt(txtPeriodicidad.getText().trim()),
                    chkCiclica.isSelected()
            );

            tipoVacunaController.registrarTipoVacuna(nuevo);
            cargarTiposVacuna();
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo guardar el tipo de vacuna: " + e.getMessage());
        }
    }

    @FXML
    public void editarTipoVacuna() {
        if (tipoEnEdicion == null) {
            mostrarAlerta("Atención", "Seleccione un tipo de vacuna de la tabla para editar.");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        try {
            tipoEnEdicion.setNombreComercial(txtNombreComercial.getText().trim());
            tipoEnEdicion.setEnfermedadQuePreviene(txtEnfermedad.getText().trim());
            tipoEnEdicion.setPeriodicidadMeses(Integer.parseInt(txtPeriodicidad.getText().trim()));
            tipoEnEdicion.setEsCiclica(chkCiclica.isSelected());

            tipoVacunaController.actualizar(tipoEnEdicion);
            cargarTiposVacuna();
            limpiarCampos();
            mostrarAlerta("Éxito", "Tipo de vacuna actualizado correctamente.");
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo actualizar el tipo de vacuna: " + e.getMessage());
        }
    }

    @FXML
    public void eliminarTipoVacuna() {
        TipoVacuna seleccionado = tablaTipoVacunas.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un tipo de vacuna de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar el tipo de vacuna " + seleccionado.getNombreComercial() + "?",
                ButtonType.YES,
                ButtonType.NO
        );
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                tipoVacunaController.eliminar(seleccionado.getId());
                cargarTiposVacuna();
                limpiarCampos();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo eliminar el tipo de vacuna: " + e.getMessage());
            }
        }
    }

    private boolean validarCampos() {
        if (txtNombreComercial.getText().trim().isEmpty()) {
            mostrarAlerta("Atención", "El nombre comercial es obligatorio.");
            return false;
        }

        if (txtEnfermedad.getText().trim().isEmpty()) {
            mostrarAlerta("Atención", "Debe indicar la enfermedad que previene.");
            return false;
        }

        int periodicidad;
        try {
            periodicidad = Integer.parseInt(txtPeriodicidad.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("Atención", "La periodicidad debe ser un número entero válido.");
            return false;
        }
        if (periodicidad <= 0) {
            mostrarAlerta("Atención", "La periodicidad debe ser mayor a cero.");
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        txtNombreComercial.clear();
        txtEnfermedad.clear();
        txtPeriodicidad.clear();
        chkCiclica.setSelected(true);
        this.tipoEnEdicion = null;
        tablaTipoVacunas.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.Especialidad;
import com.veterinaria.model.Veterinario;
import com.veterinaria.util.TextoUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Vista del módulo de veterinarios. Solo se ocupa de la interfaz: carga la
 * lista de especialidades seleccionables, arma el formulario y delega las
 * reglas de negocio a VeterinarioController.
 */
public class VeterinarioViewController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtMatricula;

    @FXML private TableView<EspecialidadSelection> tablaEspecialidades;
    @FXML private TableColumn<EspecialidadSelection, Boolean> colEspIncluir;
    @FXML private TableColumn<EspecialidadSelection, String> colEspNombre;
    @FXML private TableColumn<EspecialidadSelection, String> colEspDetalle;

    @FXML private TableView<Veterinario> tablaVeterinarios;
    @FXML private TableColumn<Veterinario, String> colMatricula;
    @FXML private TableColumn<Veterinario, String> colVetNombre;
    @FXML private TableColumn<Veterinario, String> colVetApellido;
    @FXML private TableColumn<Veterinario, String> colEspecialidades;

    private VeterinarioController veterinarioController;

    private final ObservableList<EspecialidadSelection> listaEspecialidadesSeleccionables =
            FXCollections.observableArrayList();

    private Veterinario veterinarioEnEdicion = null;

    @FXML
    public void initialize() {
        veterinarioController = new VeterinarioController(JpaUtil.getEntityManager());

        cargarEspecialidades();
        configurarColumnasEspecialidades();
        configurarColumnasVeterinarios();
        configurarValidacionesTexto();

        tablaEspecialidades.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaVeterinarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        cargarVeterinarios();

        tablaVeterinarios.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, veterinario) -> {
            if (veterinario != null) {
                cargarVeterinarioEnFormulario(veterinario);
            }
        });
    }

    private void cargarEspecialidades() {
        listaEspecialidadesSeleccionables.clear();

        for (Especialidad especialidad : veterinarioController.listarEspecialidades()) {
            listaEspecialidadesSeleccionables.add(new EspecialidadSelection(especialidad));
        }

        tablaEspecialidades.setItems(listaEspecialidadesSeleccionables);
        tablaEspecialidades.setEditable(true);
    }

    private void configurarColumnasEspecialidades() {
        colEspIncluir.setCellValueFactory(
                cellData -> cellData.getValue().selectedProperty()
        );
        colEspIncluir.setCellFactory(CheckBoxTableCell.forTableColumn(colEspIncluir));

        colEspNombre.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEspecialidad().getNombre())
        );

        colEspDetalle.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEspecialidad().getDetalle() != null
                        ? cellData.getValue().getEspecialidad().getDetalle()
                        : "")
        );
    }

    private void configurarColumnasVeterinarios() {
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colVetNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colVetApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));

        colEspecialidades.setCellValueFactory(cellData -> {
            String especialidades = cellData.getValue().getEspecialidades().stream()
                    .map(Especialidad::getNombre)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(especialidades);
        });
    }

    private void configurarValidacionesTexto() {
        for (TextField campo : List.of(txtNombre, txtApellido)) {
            campo.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches(TextoUtil.SOLO_LETRAS)) {
                    campo.setText(newVal.replaceAll(TextoUtil.NO_LETRAS, ""));
                }
            });
            campo.focusedProperty().addListener((obs, oldVal, focused) -> {
                if (!focused) {
                    campo.setText(TextoUtil.capitalizar(campo.getText()));
                }
            });
        }
    }

    private void cargarVeterinarios() {
        tablaVeterinarios.setItems(
                FXCollections.observableArrayList(veterinarioController.listarVeterinarios())
        );
    }

    private void cargarVeterinarioEnFormulario(Veterinario veterinario) {
        this.veterinarioEnEdicion = veterinario;
        txtNombre.setText(veterinario.getNombre());
        txtApellido.setText(veterinario.getApellido());
        txtMatricula.setText(veterinario.getMatricula());

        for (EspecialidadSelection item : listaEspecialidadesSeleccionables) {
            boolean incluida = veterinario.getEspecialidades().stream()
                    .anyMatch(esp -> mismoId(esp, item.getEspecialidad()));
            item.setSelected(incluida);
        }
        tablaEspecialidades.refresh();
    }

    private boolean mismoId(Especialidad a, Especialidad b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.getIdEspecialidad() != null
                && a.getIdEspecialidad().equals(b.getIdEspecialidad());
    }

    @FXML
    public void guardarVeterinario() {
        if (!validarCampos()) {
            return;
        }

        try {
            Veterinario nuevo = new Veterinario(
                    txtNombre.getText().trim(),
                    txtApellido.getText().trim(),
                    txtMatricula.getText().trim()
            );
            nuevo.getEspecialidades().addAll(especialidadesSeleccionadas());

            veterinarioController.registrarVeterinario(nuevo);
            cargarVeterinarios();
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo guardar el veterinario: " + e.getMessage());
        }
    }

    @FXML
    public void editarVeterinario() {
        if (veterinarioEnEdicion == null) {
            mostrarAlerta("Atención", "Seleccione un veterinario de la tabla para editar.");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        try {
            veterinarioEnEdicion.setNombre(txtNombre.getText().trim());
            veterinarioEnEdicion.setApellido(txtApellido.getText().trim());
            veterinarioEnEdicion.setMatricula(txtMatricula.getText().trim());
            veterinarioEnEdicion.getEspecialidades().clear();
            veterinarioEnEdicion.getEspecialidades().addAll(especialidadesSeleccionadas());

            veterinarioController.actualizar(veterinarioEnEdicion);
            cargarVeterinarios();
            limpiarCampos();
            mostrarAlerta("Éxito", "Veterinario actualizado correctamente.");
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo actualizar el veterinario: " + e.getMessage());
        }
    }

    @FXML
    public void eliminarVeterinario() {
        Veterinario seleccionado = tablaVeterinarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un veterinario de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar a " + seleccionado.getNombre() + " "
                        + seleccionado.getApellido() + "?",
                ButtonType.YES,
                ButtonType.NO
        );
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                veterinarioController.eliminar(seleccionado.getIdVeterinario());
                cargarVeterinarios();
                limpiarCampos();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo eliminar el veterinario: " + e.getMessage());
            }
        }
    }

    @FXML
    public void nuevaEspecialidad() {
        Dialog<Especialidad> dialogo = new Dialog<>();
        dialogo.setTitle("Nueva Especialidad");
        dialogo.setHeaderText("Registrar una especialidad nueva");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        TextField txtEspNombre = new TextField();
        txtEspNombre.setPromptText("Nombre de la especialidad");
        TextField txtEspDetalle = new TextField();
        txtEspDetalle.setPromptText("Detalle (opcional)");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtEspNombre, 1, 0);
        grid.add(new Label("Detalle:"), 0, 1);
        grid.add(txtEspDetalle, 1, 1);
        dialogo.getDialogPane().setContent(grid);

        dialogo.getDialogPane().lookupButton(btnGuardar).setDisable(true);
        txtEspNombre.textProperty().addListener((obs, oldVal, newVal) ->
                dialogo.getDialogPane().lookupButton(btnGuardar)
                        .setDisable(newVal.trim().isEmpty())
        );

        dialogo.setResultConverter(button -> {
            if (button == btnGuardar) {
                return new Especialidad(
                        txtEspNombre.getText().trim(),
                        txtEspDetalle.getText().trim()
                );
            }
            return null;
        });

        Optional<Especialidad> result = dialogo.showAndWait();
        result.ifPresent(especialidad -> {
            try {
                Especialidad guardada = veterinarioController.registrarEspecialidad(especialidad);
                cargarEspecialidades();
                seleccionarEspecialidad(guardada);
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo guardar la especialidad: " + e.getMessage());
            }
        });
    }

    private void seleccionarEspecialidad(Especialidad especialidad) {
        for (EspecialidadSelection item : listaEspecialidadesSeleccionables) {
            if (mismoId(item.getEspecialidad(), especialidad)) {
                item.setSelected(true);
            }
        }
        tablaEspecialidades.refresh();
    }

    private List<Especialidad> especialidadesSeleccionadas() {
        return listaEspecialidadesSeleccionables.stream()
                .filter(EspecialidadSelection::isSelected)
                .map(EspecialidadSelection::getEspecialidad)
                .toList();
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty() ||
                txtApellido.getText().trim().isEmpty() ||
                txtMatricula.getText().trim().isEmpty()) {

            mostrarAlerta("Atención", "Complete Nombre, Apellido y Matrícula.");
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtApellido.clear();
        txtMatricula.clear();
        listaEspecialidadesSeleccionables.forEach(s -> s.setSelected(false));
        tablaEspecialidades.refresh();
        this.veterinarioEnEdicion = null;
        tablaVeterinarios.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Especialidad seleccionable mediante checkbox (helper de presentación).
     */
    public static class EspecialidadSelection {

        private final Especialidad especialidad;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        public EspecialidadSelection(Especialidad especialidad) {
            this.especialidad = especialidad;
        }

        public Especialidad getEspecialidad() {
            return especialidad;
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }
    }
}

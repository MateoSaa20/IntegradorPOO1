package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

/**
 * Vista del módulo de servicios. Solo se ocupa de la interfaz: arma el
 * formulario según el tipo de servicio seleccionado y delega las reglas de
 * negocio a ServicioController.
 */
public class ServicioViewController {

    @FXML private ComboBox<TipoServicio> cmbTipo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtDuracion;
    @FXML private HBox boxTipoVacuna;
    @FXML private ComboBox<TipoVacuna> cmbTipoVacuna;

    @FXML private HBox boxCapacidad;
    @FXML private TextField txtCapacidad;

    @FXML private TableView<Servicio> tablaServicios;
    @FXML private TableColumn<Servicio, String> colNombre;
    @FXML private TableColumn<Servicio, String> colTipo;
    @FXML private TableColumn<Servicio, String> colPrecio;
    @FXML private TableColumn<Servicio, String> colDuracion;
    @FXML private TableColumn<Servicio, String> colVacuna;
    @FXML private TableColumn<Servicio, String> colCapacidad;

    private ServicioController servicioController;
    private Servicio servicioEnEdicion = null;

    @FXML
    public void initialize() {
        servicioController = new ServicioController(JpaUtil.getEntityManager());

        cmbTipo.setItems(FXCollections.observableArrayList(TipoServicio.values()));
        cmbTipo.setValue(TipoServicio.CONSULTA);

        configurarCampoTipoVacuna();
        configurarValidacionesNumericas();
        configurarColumnas();

        tablaServicios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        cargarServicios();

        tablaServicios.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, servicio) -> {
            if (servicio != null) {
                cargarServicioEnFormulario(servicio);
            }
        });
    }

    private void configurarCampoTipoVacuna() {
        List<TipoVacuna> tiposVacuna = servicioController.listarTiposVacuna();
        cmbTipoVacuna.setItems(FXCollections.observableArrayList(tiposVacuna));

        cmbTipoVacuna.setCellFactory(param -> celdaTipoVacuna());
        cmbTipoVacuna.setButtonCell(celdaTipoVacuna());

        cmbTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, tipo) -> {
            boolean esVacunacion = tipo == TipoServicio.VACUNACION;
            boxTipoVacuna.setVisible(esVacunacion);
            boxTipoVacuna.setManaged(esVacunacion);
            if (!esVacunacion) {
                cmbTipoVacuna.setValue(null);
            }

            boolean esGuarderia = tipo == TipoServicio.GUARDERIA;
            boxCapacidad.setVisible(esGuarderia);
            boxCapacidad.setManaged(esGuarderia);
            if (esGuarderia && (txtCapacidad.getText() == null || txtCapacidad.getText().isBlank())) {
                txtCapacidad.setText(String.valueOf(ServicioGuarderia.CAPACIDAD_DEFECTO));
            }
        });
    }

    private ListCell<TipoVacuna> celdaTipoVacuna() {
        return new ListCell<>() {
            @Override
            protected void updateItem(TipoVacuna item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreComercial());
            }
        };
    }

    private void configurarValidacionesNumericas() {
        txtPrecio.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*([.,]\\d*)?")) {
                txtPrecio.setText(newVal.replaceAll("[^\\d.,]", ""));
            }
        });
        txtDuracion.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtDuracion.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
        txtCapacidad.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtCapacidad.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        colTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(nombreTipo(cellData.getValue()))
        );

        colPrecio.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("$ %.2f", cellData.getValue().getPrecio()))
        );

        colDuracion.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDuracionMinutos() + " min")
        );

        colVacuna.setCellValueFactory(cellData -> {
            Servicio servicio = cellData.getValue();
            if (servicio instanceof ServicioVacunacion vacunacion
                    && vacunacion.getTipoVacuna() != null) {
                return new SimpleStringProperty(
                        vacunacion.getTipoVacuna().getNombreComercial()
                );
            }
            return new SimpleStringProperty("");
        });

        colCapacidad.setCellValueFactory(cellData -> {
            Servicio servicio = cellData.getValue();
            if (servicio instanceof ServicioGuarderia guarderia) {
                return new SimpleStringProperty(
                        guarderia.getCapacidadMaxima() + " animales"
                );
            }
            return new SimpleStringProperty("");
        });
    }

    private String nombreTipo(Servicio servicio) {
        return TipoServicio.desde(servicio).getEtiqueta();
    }

    private void cargarServicios() {
        tablaServicios.setItems(
                FXCollections.observableArrayList(servicioController.listarTodos())
        );
    }

    private void cargarServicioEnFormulario(Servicio servicio) {
        this.servicioEnEdicion = servicio;
        cmbTipo.setValue(TipoServicio.desde(servicio));
        cmbTipo.setDisable(true);
        txtNombre.setText(servicio.getNombre());
        txtPrecio.setText(String.valueOf(servicio.getPrecio()));
        txtDuracion.setText(String.valueOf(servicio.getDuracionMinutos()));

        if (servicio instanceof ServicioVacunacion vacunacion) {
            cmbTipoVacuna.setValue(vacunacion.getTipoVacuna());
        } else {
            cmbTipoVacuna.setValue(null);
        }

        if (servicio instanceof ServicioGuarderia guarderia) {
            txtCapacidad.setText(String.valueOf(guarderia.getCapacidadMaxima()));
        } else {
            txtCapacidad.clear();
        }
    }

    @FXML
    public void guardarServicio() {
        if (!validarCampos()) {
            return;
        }

        try {
            TipoServicio tipo = cmbTipo.getValue();
            Servicio nuevo = tipo.crear(
                    txtNombre.getText().trim(),
                    Double.parseDouble(txtPrecio.getText().trim().replace(',', '.')),
                    Integer.parseInt(txtDuracion.getText().trim()),
                    tipo == TipoServicio.VACUNACION ? cmbTipoVacuna.getValue() : null,
                    tipo == TipoServicio.GUARDERIA
                            ? Integer.parseInt(txtCapacidad.getText().trim())
                            : null
            );

            servicioController.registrarServicio(nuevo);
            cargarServicios();
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo guardar el servicio: " + e.getMessage());
        }
    }

    @FXML
    public void editarServicio() {
        if (servicioEnEdicion == null) {
            mostrarAlerta("Atención", "Seleccione un servicio de la tabla para editar.");
            return;
        }

        if (!validarCampos()) {
            return;
        }

        try {
            servicioEnEdicion.setNombre(txtNombre.getText().trim());
            servicioEnEdicion.setPrecio(
                    Double.parseDouble(txtPrecio.getText().trim().replace(',', '.'))
            );
            servicioEnEdicion.setDuracionMinutos(
                    Integer.parseInt(txtDuracion.getText().trim())
            );

            if (servicioEnEdicion instanceof ServicioVacunacion vacunacion) {
                vacunacion.setTipoVacuna(cmbTipoVacuna.getValue());
            }

            if (servicioEnEdicion instanceof ServicioGuarderia guarderia) {
                guarderia.setCapacidadMaxima(
                        Integer.parseInt(txtCapacidad.getText().trim())
                );
            }

            servicioController.actualizar(servicioEnEdicion);
            cargarServicios();
            limpiarCampos();
            mostrarAlerta("Éxito", "Servicio actualizado correctamente.");
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo actualizar el servicio: " + e.getMessage());
        }
    }

    @FXML
    public void eliminarServicio() {
        Servicio seleccionado = tablaServicios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un servicio de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar el servicio " + seleccionado.getNombre() + "?",
                ButtonType.YES,
                ButtonType.NO
        );
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                servicioController.eliminar(seleccionado.getIdServicio());
                cargarServicios();
                limpiarCampos();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo eliminar el servicio: " + e.getMessage());
            }
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Atención", "El nombre del servicio es obligatorio.");
            return false;
        }

        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            mostrarAlerta("Atención", "El precio debe ser un número válido.");
            return false;
        }
        if (precio <= 0) {
            mostrarAlerta("Atención", "El precio del servicio debe ser mayor a cero.");
            return false;
        }

        int duracion;
        try {
            duracion = Integer.parseInt(txtDuracion.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("Atención", "La duración debe ser un número entero válido.");
            return false;
        }
        if (duracion <= 0) {
            mostrarAlerta("Atención", "La duración del servicio debe ser mayor a cero.");
            return false;
        }

        if (cmbTipo.getValue() == TipoServicio.VACUNACION && cmbTipoVacuna.getValue() == null) {
            mostrarAlerta("Atención", "Seleccione el tipo de vacuna para el servicio de vacunación.");
            return false;
        }

        if (cmbTipo.getValue() == TipoServicio.GUARDERIA) {
            int capacidad;
            try {
                capacidad = Integer.parseInt(txtCapacidad.getText().trim());
            } catch (NumberFormatException e) {
                mostrarAlerta("Atención", "La capacidad de la guardería debe ser un número entero válido.");
                return false;
            }
            if (capacidad < 1) {
                mostrarAlerta("Atención", "La capacidad máxima de la guardería debe ser al menos 1.");
                return false;
            }
        }

        return true;
    }

    private void limpiarCampos() {
        cmbTipo.setDisable(false);
        cmbTipo.setValue(TipoServicio.CONSULTA);
        txtNombre.clear();
        txtPrecio.clear();
        txtDuracion.clear();
        txtCapacidad.clear();
        cmbTipoVacuna.setValue(null);
        this.servicioEnEdicion = null;
        tablaServicios.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Tipos de servicio seleccionables en la pantalla. Cada uno crea su
     * subclase de Servicio concreta.
     */
    private enum TipoServicio {
        CONSULTA("Consulta"),
        VACUNACION("Vacunación"),
        GUARDERIA("Guardería"),
        PELUQUERIA("Peluquería");

        private final String etiqueta;

        TipoServicio(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }

        public Servicio crear(String nombre, double precio, int duracion, TipoVacuna vacuna, Integer capacidad) {
            return switch (this) {
                case VACUNACION -> new ServicioVacunacion(nombre, precio, duracion, vacuna);
                case GUARDERIA -> new ServicioGuarderia(nombre, precio, duracion, capacidad);
                case PELUQUERIA -> new ServicioPeluqueria(nombre, precio, duracion);
                default -> new ServicioConsulta(nombre, precio, duracion);
            };
        }

        public static TipoServicio desde(Servicio servicio) {
            if (servicio instanceof ServicioVacunacion) {
                return VACUNACION;
            }
            if (servicio instanceof ServicioGuarderia) {
                return GUARDERIA;
            }
            if (servicio instanceof ServicioPeluqueria) {
                return PELUQUERIA;
            }
            return CONSULTA;
        }
    }
}

package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.Mascota;
import com.veterinaria.model.ServicioVacunacion;
import com.veterinaria.model.Veterinario;
import com.veterinaria.service.AlertaVacunacion;
import com.veterinaria.service.EstadoVacuna;
import com.veterinaria.service.VacunacionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Pantalla de control de vacunaciones: lista las mascotas con vacunas
 * vencidas o por vencer y permite registrar una nueva vacunación. Las
 * reglas de negocio (próxima dosis, periodicidad) las aplica
 * VacunacionController.
 */
public class VacunacionViewController {

    @FXML private TableView<AlertaVacunacion> tablaAlertas;
    @FXML private TableColumn<AlertaVacunacion, String> colMascota;
    @FXML private TableColumn<AlertaVacunacion, String> colEspecie;
    @FXML private TableColumn<AlertaVacunacion, String> colDueno;
    @FXML private TableColumn<AlertaVacunacion, String> colVacuna;
    @FXML private TableColumn<AlertaVacunacion, String> colEnfermedad;
    @FXML private TableColumn<AlertaVacunacion, String> colUltima;
    @FXML private TableColumn<AlertaVacunacion, String> colProxima;
    @FXML private TableColumn<AlertaVacunacion, String> colDias;
    @FXML private TableColumn<AlertaVacunacion, String> colEstado;

    @FXML private VBox panelFormulario;
    @FXML private ComboBox<Mascota> cmbMascota;
    @FXML private ComboBox<ServicioVacunacion> cmbVacuna;
    @FXML private ComboBox<Veterinario> cmbVeterinario;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private TextField txtLaboratorio;
    @FXML private TextField txtObservacionesDosis;

    private VacunacionController vacunacionController;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        vacunacionController = new VacunacionController(new VacunacionService(JpaUtil.getEntityManager()));

        configurarColumnas();
        cargarFormulario();
        cargarAlertas();

        // Seleccionar una fila de alerta precarga el formulario con la mascota.
        tablaAlertas.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, alerta) -> {
                    if (alerta != null) {
                        cmbMascota.setValue(alerta.mascota());
                        mostrarFormulario();
                    }
                });
    }

    // ==========================================================
    // CARGA DE DATOS
    // ==========================================================

    private void cargarAlertas() {
        tablaAlertas.setItems(
                FXCollections.observableArrayList(vacunacionController.listarVacunasEnAlerta())
        );
    }

    private void cargarFormulario() {
        cmbMascota.setItems(
                FXCollections.observableArrayList(vacunacionController.listarMascotas())
        );
        cmbMascota.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Mascota item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : textoMascota(item));
            }
        });
        cmbMascota.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Mascota item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : textoMascota(item));
            }
        });

        cmbVacuna.setItems(
                FXCollections.observableArrayList(vacunacionController.listarServiciosVacunacion())
        );
        cmbVacuna.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ServicioVacunacion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });
        cmbVacuna.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ServicioVacunacion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombre());
            }
        });

        cmbVeterinario.setItems(
                FXCollections.observableArrayList(vacunacionController.listarVeterinarios())
        );
        cmbVeterinario.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Veterinario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : textoVeterinario(item));
            }
        });
        cmbVeterinario.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Veterinario item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : textoVeterinario(item));
            }
        });
    }

    private String textoMascota(Mascota mascota) {
        return mascota.getNombre()
                + " (" + mascota.getEspecie().getNombre()
                + " - Ficha " + mascota.getNumeroFicha() + ")";
    }

    private String textoVeterinario(Veterinario veterinario) {
        return "Mat. " + veterinario.getMatricula()
                + " - " + veterinario.getNombre() + " " + veterinario.getApellido();
    }

    // ==========================================================
    // COLUMNAS
    // ==========================================================

    private void configurarColumnas() {
        colMascota.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMascotaNombre()));
        colEspecie.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEspecie()));
        colDueno.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDueno()));
        colVacuna.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().estado().getNombreVacuna()));
        colEnfermedad.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().estado().getEnfermedad()));
        colUltima.setCellValueFactory(cellData ->
                new SimpleStringProperty(fechaOguion(cellData.getValue().estado().ultimaAplicacion())));
        colProxima.setCellValueFactory(cellData ->
                new SimpleStringProperty(fechaOguion(cellData.getValue().estado().proximaAplicacion())));
        colDias.setCellValueFactory(cellData -> {
            EstadoVacuna estado = cellData.getValue().estado();
            if (estado.proximaAplicacion() == null) {
                return new SimpleStringProperty("");
            }
            long dias = estado.diasParaProxima();
            return new SimpleStringProperty(dias < 0 ? "vencida" : dias + " días");
        });
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().estado().getEstado()));
    }

    private String fechaOguion(LocalDate fecha) {
        return fecha != null ? fecha.format(dateFormatter) : "—";
    }

    // ==========================================================
    // ACCIONES
    // ==========================================================

    @FXML
    public void mostrarFormulario() {
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    @FXML
    public void ocultarFormulario() {
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
        limpiarFormulario();
        tablaAlertas.getSelectionModel().clearSelection();
    }

    @FXML
    public void registrar() {
        Mascota mascota = cmbMascota.getValue();
        ServicioVacunacion vacuna = cmbVacuna.getValue();
        Veterinario veterinario = cmbVeterinario.getValue();
        LocalDate fecha = dpFecha.getValue();

        try {
            if (mascota == null || vacuna == null || veterinario == null || fecha == null) {
                mostrarAlerta("Atención",
                        "Debe completar la mascota, la vacuna, el veterinario y la fecha.");
                return;
            }

            LocalDateTime fechaHora = fecha.atTime(parsearHora());

            vacunacionController.registrarVacunacion(
                    mascota,
                    vacuna,
                    veterinario,
                    fechaHora,
                    textoOguion(txtLaboratorio.getText()),
                    textoOguion(txtObservacionesDosis.getText())
            );

            mostrarAlerta("Registro exitoso",
                    "Vacunación " + vacuna.getTipoVacuna().getNombreComercial()
                            + " registrada para " + mascota.getNombre() + ".");
            ocultarFormulario();
            cargarAlertas();
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    private LocalTime parsearHora() throws Exception {
        String texto = txtHora.getText() == null ? "" : txtHora.getText().trim();
        if (texto.isEmpty()) {
            return LocalTime.of(10, 0);
        }
        try {
            return LocalTime.parse(texto, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new Exception("La hora debe tener el formato HH:mm (ej: 10:00).");
        }
    }

    private String textoOguion(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private void limpiarFormulario() {
        cmbMascota.getSelectionModel().clearSelection();
        cmbVacuna.getSelectionModel().clearSelection();
        cmbVeterinario.getSelectionModel().clearSelection();
        dpFecha.setValue(null);
        txtHora.clear();
        txtLaboratorio.clear();
        txtObservacionesDosis.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

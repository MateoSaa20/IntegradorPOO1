package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.Cliente;
import com.veterinaria.model.DetalleConsulta;
import com.veterinaria.model.DetalleVacunacion;
import com.veterinaria.model.Mascota;
import com.veterinaria.model.ItemGuarderia;
import com.veterinaria.model.Turno;
import com.veterinaria.model.Veterinario;
import com.veterinaria.service.EstadoVacuna;
import com.veterinaria.service.HistorialService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Vista del historial y vacunas. Busca al dueño por DNI, permite elegir una
 * de sus mascotas y muestra sus atenciones y el estado de sus vacunas. Las
 * reglas de negocio (próxima dosis, alerta a menos de un mes) las aplica
 * HistorialController.
 */
public class HistorialViewController {

    @FXML private TextField txtDni;
    @FXML private Label lblCliente;
    @FXML private ComboBox<Mascota> cmbMascota;
    @FXML private Label lblAviso;

    @FXML private TableView<Turno> tablaAtenciones;
    @FXML private TableColumn<Turno, String> colAtencionFecha;
    @FXML private TableColumn<Turno, String> colAtencionVeterinario;
    @FXML private TableColumn<Turno, String> colAtencionServicios;
    @FXML private TableColumn<Turno, String> colAtencionDetalle;

    @FXML private TableView<EstadoVacuna> tablaEstadoVacunas;
    @FXML private TableColumn<EstadoVacuna, String> colEstVacuna;
    @FXML private TableColumn<EstadoVacuna, String> colEstEnfermedad;
    @FXML private TableColumn<EstadoVacuna, String> colEstUltima;
    @FXML private TableColumn<EstadoVacuna, String> colEstProxima;
    @FXML private TableColumn<EstadoVacuna, String> colEstDias;
    @FXML private TableColumn<EstadoVacuna, String> colEstEstado;

    @FXML private TableView<DetalleVacunacion> tablaAplicaciones;
    @FXML private TableColumn<DetalleVacunacion, String> colAplFecha;
    @FXML private TableColumn<DetalleVacunacion, String> colAplVacuna;
    @FXML private TableColumn<DetalleVacunacion, String> colAplLaboratorio;
    @FXML private TableColumn<DetalleVacunacion, String> colAplDosis;

    private HistorialController historialController;
    private Cliente clienteSeleccionado;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        historialController = new HistorialController(new HistorialService(JpaUtil.getEntityManager()));

        txtDni.textProperty().addListener((obs, oldVal, newVal) -> {
            String soloValido = newVal.replaceAll("[^\\d.]", "");
            if (!soloValido.equals(newVal)) {
                txtDni.setText(soloValido);
            }
        });

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

        configurarColumnasAtenciones();
        configurarColumnasEstadoVacunas();
        configurarColumnasAplicaciones();

        cmbMascota.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, mascota) -> cargarMascota(mascota));
    }

    private String textoMascota(Mascota mascota) {
        return mascota.getNombre()
                + " (" + mascota.getEspecie().getNombre()
                + " - Ficha " + mascota.getNumeroFicha() + ")";
    }

    // ==========================================================
    // COLUMNAS
    // ==========================================================

    private void configurarColumnasAtenciones() {
        colAtencionFecha.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getFechaHora();
            return new SimpleStringProperty(fecha != null ? fecha.format(dateTimeFormatter) : "");
        });

        colAtencionVeterinario.setCellValueFactory(cellData -> {
            Veterinario vet = cellData.getValue().getVeterinario();
            return new SimpleStringProperty(vet != null
                    ? "Mat. " + vet.getMatricula() + " - " + vet.getNombre() + " " + vet.getApellido()
                    : "");
        });

        colAtencionServicios.setCellValueFactory(cellData ->
                new SimpleStringProperty(serviciosDeTurno(cellData.getValue()))
        );

        colAtencionDetalle.setCellValueFactory(cellData ->
                new SimpleStringProperty(detallesDeTurno(cellData.getValue()))
        );
    }

    private void configurarColumnasEstadoVacunas() {
        colEstVacuna.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombreVacuna()));
        colEstEnfermedad.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEnfermedad()));
        colEstUltima.setCellValueFactory(cellData ->
                new SimpleStringProperty(fechaOguion(cellData.getValue().ultimaAplicacion())));
        colEstProxima.setCellValueFactory(cellData ->
                new SimpleStringProperty(fechaOguion(cellData.getValue().proximaAplicacion())));
        colEstDias.setCellValueFactory(cellData -> {
            EstadoVacuna estado = cellData.getValue();
            if (estado.proximaAplicacion() == null) {
                return new SimpleStringProperty("");
            }
            long dias = estado.diasParaProxima();
            return new SimpleStringProperty(dias < 0 ? "vencida" : dias + " días");
        });
        colEstEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstado()));
    }

    private void configurarColumnasAplicaciones() {
        colAplFecha.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getItemTurno().getTurno().getFechaHora();
            return new SimpleStringProperty(fecha != null ? fecha.format(dateTimeFormatter) : "");
        });
        colAplVacuna.setCellValueFactory(cellData -> {
            DetalleVacunacion dv = cellData.getValue();
            return new SimpleStringProperty(dv.getTipoVacuna() != null
                    ? dv.getTipoVacuna().getNombreComercial()
                    : "");
        });
        colAplLaboratorio.setCellValueFactory(cellData ->
                new SimpleStringProperty(vacioSiNull(cellData.getValue().getLaboratorioOMarca())));
        colAplDosis.setCellValueFactory(cellData ->
                new SimpleStringProperty(vacioSiNull(cellData.getValue().getObservacionesDosis())));
    }

    private String fechaOguion(java.time.LocalDate fecha) {
        return fecha != null ? fecha.format(dateFormatter) : "—";
    }

    private String vacioSiNull(String texto) {
        return texto == null ? "" : texto;
    }

    // ==========================================================
    // CONSTRUCCIÓN DE TEXTOS
    // ==========================================================

    private String serviciosDeTurno(Turno turno) {
        return turno.getItems().stream()
                .map(item -> {
                    if (item instanceof ItemGuarderia guarderia
                            && guarderia.getFechaHoraInicio() != null
                            && guarderia.getFechaHoraFin() != null) {
                        return item.getServicio().getNombre()
                                + " (" + guarderia.getFechaHoraInicio().toLocalDate().format(dateFormatter)
                                + " - " + guarderia.getFechaHoraFin().toLocalDate().format(dateFormatter) + ")";
                    }
                    return item.getServicio().getNombre();
                })
                .collect(Collectors.joining(", "));
    }

    private String detallesDeTurno(Turno turno) {
        return turno.getItems().stream()
                .map(item -> {
                    if (item.getDetalleAtencion() == null) {
                        return null;
                    }
                    if (item.getDetalleAtencion() instanceof DetalleConsulta consulta) {
                        return "Consulta: " + consulta.getDiagnostico();
                    }
                    if (item.getDetalleAtencion() instanceof DetalleVacunacion vacuna
                            && vacuna.getTipoVacuna() != null) {
                        return "Vacuna: " + vacuna.getTipoVacuna().getNombreComercial();
                    }
                    return vacioSiNull(item.getDetalleAtencion().getObservaciones());
                })
                .filter(texto -> texto != null && !texto.isBlank())
                .collect(Collectors.joining(" | "));
    }

    // ==========================================================
    // ACCIONES
    // ==========================================================

    @FXML
    public void buscarCliente() {
        String dni = txtDni.getText().trim();

        if (dni.isEmpty()) {
            mostrarAlerta("Atención", "Ingrese el DNI del dueño para buscar.");
            return;
        }

        Optional<Cliente> resultado = historialController.buscarClientePorDni(dni);

        if (resultado.isEmpty()) {
            mostrarAlerta("Sin resultados", "No se encontró un dueño con DNI " + dni + ".");
            limpiarPantalla();
            return;
        }

        clienteSeleccionado = resultado.get();
        lblCliente.setText(clienteSeleccionado.getNombre() + " " + clienteSeleccionado.getApellido()
                + " · Tel: " + clienteSeleccionado.getTelefono());

        List<Mascota> mascotas = historialController.listarMascotas(clienteSeleccionado);
        cmbMascota.setItems(FXCollections.observableArrayList(mascotas));

        if (mascotas.isEmpty()) {
            mostrarAlerta("Atención", "El dueño no tiene mascotas registradas.");
            limpiarDatosMascota();
        } else {
            cmbMascota.getSelectionModel().selectFirst();
        }
    }

    private void cargarMascota(Mascota mascota) {
        if (mascota == null) {
            limpiarDatosMascota();
            return;
        }

        tablaAtenciones.setItems(
                FXCollections.observableArrayList(historialController.listarAtenciones(mascota))
        );

        List<EstadoVacuna> estados = historialController.calcularEstadoVacunas(mascota);
        tablaEstadoVacunas.setItems(FXCollections.observableArrayList(estados));

        tablaAplicaciones.setItems(
                FXCollections.observableArrayList(historialController.listarVacunas(mascota))
        );

        mostrarBannerAviso(mascota);
    }

    private void mostrarBannerAviso(Mascota mascota) {
        List<EstadoVacuna> porVencer = historialController.vacunasPorVencer(mascota);

        if (porVencer.isEmpty()) {
            ocultarBanner();
            return;
        }

        String detalle = porVencer.stream()
                .map(this::formatearAviso)
                .collect(Collectors.joining(", "));

        lblAviso.setText("Vacunas por vencer: " + detalle);
        lblAviso.setVisible(true);
        lblAviso.setManaged(true);
    }

    private String formatearAviso(EstadoVacuna estado) {
        long dias = estado.diasParaProxima();
        if (dias < 0) {
            return estado.getNombreVacuna() + " (vencida hace " + (-dias) + " días)";
        }
        if (dias == 0) {
            return estado.getNombreVacuna() + " (vence hoy)";
        }
        return estado.getNombreVacuna() + " (faltan " + dias + " días)";
    }

    private void limpiarPantalla() {
        lblCliente.setText("");
        clienteSeleccionado = null;
        cmbMascota.getItems().clear();
        cmbMascota.getSelectionModel().clearSelection();
        limpiarDatosMascota();
    }

    private void limpiarDatosMascota() {
        ocultarBanner();
        tablaAtenciones.getItems().clear();
        tablaEstadoVacunas.getItems().clear();
        tablaAplicaciones.getItems().clear();
    }

    private void ocultarBanner() {
        lblAviso.setText("");
        lblAviso.setVisible(false);
        lblAviso.setManaged(false);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

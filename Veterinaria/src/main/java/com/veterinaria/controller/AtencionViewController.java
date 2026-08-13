package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.*;
import com.veterinaria.service.AtencionService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Vista del módulo de atención de turnos. Solo se ocupa de la interfaz:
 * lista los turnos CONFIRMADO, muestra sus servicios y permite cargar el
 * detalle de cada uno (según el tipo de servicio) antes de atender. Las
 * reglas de negocio las aplica AtencionController.
 */
public class AtencionViewController {

    @FXML private TableView<Turno> tablaTurnos;
    @FXML private TableColumn<Turno, String> colTurnoId;
    @FXML private TableColumn<Turno, String> colTurnoFechaHora;
    @FXML private TableColumn<Turno, String> colTurnoCliente;
    @FXML private TableColumn<Turno, String> colTurnoMascota;
    @FXML private TableColumn<Turno, String> colTurnoVeterinario;
    @FXML private TableColumn<Turno, String> colTurnoServicios;

    @FXML private TableView<ItemTurno> tablaItems;
    @FXML private TableColumn<ItemTurno, String> colItemServicio;
    @FXML private TableColumn<ItemTurno, String> colItemTipo;
    @FXML private TableColumn<ItemTurno, String> colItemPrecio;
    @FXML private TableColumn<ItemTurno, String> colItemDuracion;
    @FXML private TableColumn<ItemTurno, String> colItemEstadoDetalle;

    @FXML private Label lblServicioDetalle;
    @FXML private TextArea txtObservaciones;
    @FXML private HBox boxConsulta;
    @FXML private TextField txtDiagnostico;
    @FXML private VBox boxTratamientos;
    @FXML private TableView<Tratamiento> tablaTratamientos;
    @FXML private TableColumn<Tratamiento, String> colTratInicio;
    @FXML private TableColumn<Tratamiento, String> colTratFin;
    @FXML private TableColumn<Tratamiento, String> colTratDescripcion;
    @FXML private DatePicker dpTratInicio;
    @FXML private DatePicker dpTratFin;
    @FXML private TextField txtTratDescripcion;
    @FXML private VBox boxVacuna;
    @FXML private Label lblTipoVacuna;
    @FXML private TextField txtLaboratorio;
    @FXML private TextField txtObservacionesDosis;
    @FXML private Label lblMensaje;

    private AtencionController atencionController;

    private Turno turnoSeleccionado;
    private ItemTurno itemSeleccionado;
    private Tratamiento tratamientoEnEdicion;

    private final ObservableList<Tratamiento> listaTratamientos =
            FXCollections.observableArrayList();

    private final Map<Long, DetalleAtencion> detallesPorItem =
            new HashMap<>();

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ==========================================================
    // INITIALIZE
    // ==========================================================

    @FXML
    public void initialize() {

        atencionController = new AtencionController(new AtencionService(JpaUtil.getEntityManager()));

        configurarColumnasTurnos();
        configurarColumnasItems();
        configurarColumnasTratamientos();

        tablaTurnos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        tablaItems.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        tablaTratamientos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tablaTratamientos.setItems(listaTratamientos);

        cargarTurnos();

        tablaTurnos.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, turno) -> {
                    turnoSeleccionado = turno;
                    cargarItemsTurno(turno);
                });

        tablaItems.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, item) -> {
                    itemSeleccionado = item;
                    cargarFormulario(item);
                });

        tablaTratamientos.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, tratamiento) -> {
                    tratamientoEnEdicion = tratamiento;
                    if (tratamiento != null) {
                        dpTratInicio.setValue(tratamiento.getFechaInicio());
                        dpTratFin.setValue(tratamiento.getFechaFin());
                        txtTratDescripcion.setText(tratamiento.getDescripcion());
                    }
                });
    }

    // ==========================================================
    // COLUMNAS DE TURNOS
    // ==========================================================

    private void configurarColumnasTurnos() {

        colTurnoId.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.valueOf(cellData.getValue().getIdTurno())
                )
        );

        colTurnoFechaHora.setCellValueFactory(cellData -> {

            LocalDateTime fecha =
                    cellData.getValue().getFechaHora();

            return new SimpleStringProperty(
                    fecha != null
                            ? fecha.format(dateTimeFormatter)
                            : ""
            );
        });

        colTurnoCliente.setCellValueFactory(cellData -> {

            Mascota mascota =
                    cellData.getValue().getMascota();

            if (mascota != null
                    && mascota.getCliente() != null) {

                Cliente cliente = mascota.getCliente();

                return new SimpleStringProperty(
                        cliente.getNombre()
                                + " "
                                + cliente.getApellido()
                );
            }

            return new SimpleStringProperty("");
        });

        colTurnoMascota.setCellValueFactory(cellData -> {

            Mascota mascota =
                    cellData.getValue().getMascota();

            return new SimpleStringProperty(
                    mascota != null ? mascota.getNombre() : ""
            );
        });

        colTurnoVeterinario.setCellValueFactory(cellData -> {

            Veterinario veterinario =
                    cellData.getValue().getVeterinario();

            if (veterinario != null) {

                return new SimpleStringProperty(
                        "Mat. "
                                + veterinario.getMatricula()
                                + " - "
                                + veterinario.getNombre()
                );
            }

            return new SimpleStringProperty("");
        });

        colTurnoServicios.setCellValueFactory(cellData -> {

            Turno turno = cellData.getValue();

            String servicios = turno.getItems().stream()
                    .map(item -> {

                        String nombre =
                                item.getServicio().getNombre();

                        if (item instanceof ItemGuarderia guarderia) {

                            return nombre
                                    + " ("
                                    + guarderia.getFechaHoraInicio()
                                            .toLocalDate()
                                            .format(dateFormatter)
                                    + " - "
                                    + guarderia.getFechaHoraFin()
                                            .toLocalDate()
                                            .format(dateFormatter)
                                    + ")";
                        }

                        return nombre;
                    })
                    .collect(Collectors.joining(", "));

            return new SimpleStringProperty(servicios);
        });
    }

    // ==========================================================
    // COLUMNAS DE ITEMS
    // ==========================================================

    private void configurarColumnasItems() {

        colItemServicio.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue()
                                .getServicio()
                                .getNombre()
                )
        );

        colItemTipo.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        nombreTipo(cellData.getValue().getServicio())
                )
        );

        colItemPrecio.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.format(
                                "$ %.2f",
                                cellData.getValue().getPrecioAlMomento()
                        )
                )
        );

        colItemDuracion.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getTiempoAlMomento()
                                + " min"
                )
        );

        colItemEstadoDetalle.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDetalleAtencion() != null
                                ? "Cargado"
                                : "Sin detalle"
                )
        );
    }

    private String nombreTipo(Servicio servicio) {

        if (servicio instanceof ServicioConsulta) {
            return "Consulta";
        }
        if (servicio instanceof ServicioVacunacion) {
            return "Vacunación";
        }
        if (servicio instanceof ServicioGuarderia) {
            return "Guardería";
        }
        if (servicio instanceof ServicioPeluqueria) {
            return "Peluquería";
        }
        return "";
    }

    // ==========================================================
    // COLUMNAS DE TRATAMIENTOS
    // ==========================================================

    private void configurarColumnasTratamientos() {

        colTratInicio.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getFechaInicio()
                                .format(dateFormatter)
                )
        );

        colTratFin.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getFechaFin()
                                .format(dateFormatter)
                )
        );

        colTratDescripcion.setCellValueFactory(
                new PropertyValueFactory<>("descripcion")
        );
    }

    // ==========================================================
    // CARGA DE DATOS
    // ==========================================================

    private void cargarTurnos() {

        tablaTurnos.setItems(
                FXCollections.observableArrayList(
                        atencionController.listarTurnosConfirmados()
                )
        );
    }

    private void cargarItemsTurno(Turno turno) {

        detallesPorItem.clear();
        tablaItems.getSelectionModel().clearSelection();

        if (turno == null) {
            tablaItems.setItems(FXCollections.observableArrayList());
            limpiarFormulario();
            return;
        }

        tablaItems.setItems(
                FXCollections.observableArrayList(turno.getItems())
        );

        limpiarFormulario();
    }

    // ==========================================================
    // FORMULARIO ADAPTABLE
    // ==========================================================

    private void cargarFormulario(ItemTurno item) {

        limpiarFormulario();

        if (item == null) {
            return;
        }

        lblServicioDetalle.setText(
                item.getServicio().getNombre()
        );

        Servicio servicio = item.getServicio();

        DetalleAtencion detalle =
                detallesPorItem.get(item.getIdItem());

        if (detalle == null) {
            detalle = item.getDetalleAtencion();
        }

        if (servicio instanceof ServicioConsulta) {

            boxConsulta.setVisible(true);
            boxConsulta.setManaged(true);
            boxTratamientos.setVisible(true);
            boxTratamientos.setManaged(true);

            if (detalle instanceof DetalleConsulta consulta) {

                txtObservaciones.setText(vacioSiNull(consulta.getObservaciones()));
                txtDiagnostico.setText(vacioSiNull(consulta.getDiagnostico()));
                listaTratamientos.setAll(consulta.getTratamientos());
            }

        } else if (servicio instanceof ServicioVacunacion vacunacion) {

            boxVacuna.setVisible(true);
            boxVacuna.setManaged(true);

            if (vacunacion.getTipoVacuna() != null) {

                lblTipoVacuna.setText(
                        vacunacion.getTipoVacuna().getNombreComercial()
                );
            }

            if (detalle instanceof DetalleVacunacion vacuna) {

                txtObservaciones.setText(vacioSiNull(vacuna.getObservaciones()));
                txtLaboratorio.setText(vacioSiNull(vacuna.getLaboratorioOMarca()));
                txtObservacionesDosis.setText(
                        vacioSiNull(vacuna.getObservacionesDosis())
                );
            }

        } else {

            if (detalle != null) {

                txtObservaciones.setText(
                        vacioSiNull(detalle.getObservaciones())
                );
            }
        }
    }

    private String vacioSiNull(String texto) {
        return texto == null ? "" : texto;
    }

    private void limpiarFormulario() {

        lblServicioDetalle.setText("(seleccione un servicio)");
        lblMensaje.setText("");

        txtObservaciones.clear();
        txtDiagnostico.clear();
        txtLaboratorio.clear();
        txtObservacionesDosis.clear();
        lblTipoVacuna.setText("");

        listaTratamientos.clear();
        limpiarEditorTratamiento();

        boxConsulta.setVisible(false);
        boxConsulta.setManaged(false);
        boxTratamientos.setVisible(false);
        boxTratamientos.setManaged(false);
        boxVacuna.setVisible(false);
        boxVacuna.setManaged(false);
    }

    private void limpiarEditorTratamiento() {

        tratamientoEnEdicion = null;
        dpTratInicio.setValue(null);
        dpTratFin.setValue(null);
        txtTratDescripcion.clear();
        tablaTratamientos.getSelectionModel().clearSelection();
    }

    // ==========================================================
    // EDITOR DE TRATAMIENTOS
    // ==========================================================

    @FXML
    public void agregarTratamiento() {

        LocalDate inicio = dpTratInicio.getValue();
        LocalDate fin = dpTratFin.getValue();
        String descripcion = txtTratDescripcion.getText().trim();

        if (inicio == null || fin == null || descripcion.isEmpty()) {

            mostrarAlerta(
                    "Atención",
                    "Complete inicio, fin y descripción del tratamiento."
            );

            return;
        }

        Tratamiento candidato =
                new Tratamiento(inicio, fin, descripcion);

        try {

            candidato.validar();

        } catch (IllegalArgumentException e) {

            mostrarAlerta("Atención", e.getMessage());
            return;
        }

        if (tratamientoEnEdicion != null) {

            tratamientoEnEdicion.setFechaInicio(inicio);
            tratamientoEnEdicion.setFechaFin(fin);
            tratamientoEnEdicion.setDescripcion(descripcion);
            tablaTratamientos.refresh();

        } else {

            listaTratamientos.add(candidato);
        }

        limpiarEditorTratamiento();
    }

    @FXML
    public void quitarTratamiento() {

        Tratamiento seleccionado =
                tablaTratamientos.getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {

            mostrarAlerta(
                    "Atención",
                    "Seleccione un tratamiento de la tabla."
            );

            return;
        }

        listaTratamientos.remove(seleccionado);
        limpiarEditorTratamiento();
    }

    // ==========================================================
    // CONSTRUCCIÓN Y VALIDACIÓN DEL DETALLE
    // ==========================================================

    /**
     * El detalle se construye SIEMPRE nuevo (con copias de los tratamientos):
     * el controller reemplaza el detalle anterior sobre el item y el
     * orphanRemoval se encarga de eliminar el registro previo.
     */
    private DetalleAtencion construirDetalle(ItemTurno item) {

        Servicio servicio = item.getServicio();

        if (servicio instanceof ServicioConsulta) {

            DetalleConsulta detalle = new DetalleConsulta();
            detalle.setObservaciones(txtObservaciones.getText().trim());
            detalle.setDiagnostico(txtDiagnostico.getText().trim());

            List<Tratamiento> tratamientos = new ArrayList<>();

            for (Tratamiento tratamiento : listaTratamientos) {

                tratamientos.add(new Tratamiento(
                        tratamiento.getFechaInicio(),
                        tratamiento.getFechaFin(),
                        tratamiento.getDescripcion()
                ));
            }

            detalle.setTratamientos(tratamientos);
            return detalle;
        }

        if (servicio instanceof ServicioVacunacion vacunacion) {

            DetalleVacunacion detalle = new DetalleVacunacion();
            detalle.setObservaciones(txtObservaciones.getText().trim());
            detalle.setTipoVacuna(vacunacion.getTipoVacuna());
            detalle.setLaboratorioOMarca(txtLaboratorio.getText().trim());
            detalle.setObservacionesDosis(
                    txtObservacionesDosis.getText().trim()
            );
            return detalle;
        }

        return new DetalleAtencion(txtObservaciones.getText().trim());
    }

    private boolean formularioVacio(ItemTurno item) {

        if (!txtObservaciones.getText().isBlank()) {
            return false;
        }

        if (item.getServicio() instanceof ServicioConsulta
                && !txtDiagnostico.getText().isBlank()) {
            return false;
        }

        if (item.getServicio() instanceof ServicioVacunacion
                && (!txtLaboratorio.getText().isBlank()
                    || !txtObservacionesDosis.getText().isBlank())) {
            return false;
        }

        return true;
    }

    // ==========================================================
    // GUARDAR DETALLES
    // ==========================================================

    @FXML
    public void guardarDetalles() {

        if (turnoSeleccionado == null) {

            mostrarAlerta(
                    "Atención",
                    "Seleccione un turno confirmado."
            );

            return;
        }

        if (itemSeleccionado == null) {

            mostrarAlerta(
                    "Atención",
                    "Seleccione un servicio del turno."
            );

            return;
        }

        if (formularioVacio(itemSeleccionado)) {

            mostrarAlerta(
                    "Sin detalle",
                    "El detalle es opcional: complete el formulario para "
                            + "registrar el detalle de \""
                            + itemSeleccionado.getServicio().getNombre()
                            + "\"."
            );

            return;
        }

        DetalleAtencion detalle = construirDetalle(itemSeleccionado);

        try {

            detalle.validar();

        } catch (IllegalArgumentException e) {

            lblMensaje.setText(e.getMessage());
            return;
        }

        detallesPorItem.put(itemSeleccionado.getIdItem(), detalle);

        try {

            atencionController.guardarDetalles(
                    turnoSeleccionado.getIdTurno(),
                    detallesPorItem
            );

            lblMensaje.setText("Detalles guardados correctamente.");
            tablaItems.refresh();

        } catch (Exception e) {

            mostrarAlerta(
                    "Error",
                    "No se pudieron guardar los detalles: " + e.getMessage()
            );
        }
    }

    // ==========================================================
    // ATENDER TURNO
    // ==========================================================

    @FXML
    public void atenderTurno() {

        if (turnoSeleccionado == null) {

            mostrarAlerta(
                    "Atención",
                    "Seleccione un turno confirmado."
            );

            return;
        }

        // Si hay un servicio con el formulario cargado, se persiste su
        // detalle junto con el resto de los detalles en memoria.
        if (itemSeleccionado != null
                && !formularioVacio(itemSeleccionado)) {

            DetalleAtencion detalle =
                    construirDetalle(itemSeleccionado);

            try {

                detalle.validar();

            } catch (IllegalArgumentException e) {

                lblMensaje.setText(e.getMessage());
                return;
            }

            detallesPorItem.put(
                    itemSeleccionado.getIdItem(),
                    detalle
            );
        }

        try {

            atencionController.atenderTurno(
                    turnoSeleccionado.getIdTurno(),
                    detallesPorItem
            );

            mostrarAlerta(
                    "Éxito",
                    "El turno fue atendido y sus detalles guardados."
            );

            cargarTurnos();
            resetearPantalla();

        } catch (Exception e) {

            mostrarAlerta(
                    "Error",
                    "No se pudo atender el turno: " + e.getMessage()
            );
        }
    }

    // ==========================================================
    // LIMPIEZA
    // ==========================================================

    private void resetearPantalla() {

        turnoSeleccionado = null;
        itemSeleccionado = null;

        detallesPorItem.clear();

        tablaTurnos.getSelectionModel().clearSelection();
        tablaItems.getSelectionModel().clearSelection();

        limpiarFormulario();
    }

    // ==========================================================
    // ALERTAS
    // ==========================================================

    private void mostrarAlerta(String titulo, String mensaje) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

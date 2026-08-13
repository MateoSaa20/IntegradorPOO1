package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.*;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Vista del módulo de turnos. Solo se ocupa de la interfaz: carga los
 * combos y tablas a través de TurnoController, arma los datos del
 * formulario y delega las reglas de negocio al controlador/modelo.
 */
public class TurnoViewController {

    @FXML private TextField txtDniCliente;
    @FXML private Label lblClienteEncontrado;
    @FXML private ComboBox<Mascota> cmbMascota;
    @FXML private ComboBox<Veterinario> cmbVeterinario;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private ComboBox<EstadoTurno> cmbEstado;

    @FXML private TableView<Turno> tablaTurnos;
    @FXML private TableColumn<Turno, Long> colId;
    @FXML private TableColumn<Turno, String> colFechaHora;
    @FXML private TableColumn<Turno, String> colCliente;
    @FXML private TableColumn<Turno, String> colMascota;
    @FXML private TableColumn<Turno, String> colVeterinario;
    @FXML private TableColumn<Turno, EstadoTurno> colEstado;
    @FXML private TableColumn<Turno, String> colDuracion;
    @FXML private TableColumn<Turno, String> colPrecioTotal;
    @FXML private TableColumn<Turno, String> colServicios;

    @FXML private TableView<ServicioSelection> tablaSeleccionServicios;
    @FXML private TableColumn<ServicioSelection, Boolean> colServicioIncluir;
    @FXML private TableColumn<ServicioSelection, String> colServicioNombre;
    @FXML private TableColumn<ServicioSelection, String> colServicioDuracion;
    @FXML private TableColumn<ServicioSelection, String> colServicioPrecio;

    @FXML private HBox boxGuarderia;
    @FXML private DatePicker dpIngresoGuarderia;
    @FXML private DatePicker dpSalidaGuarderia;
    @FXML private TextField txtHoraSalida;

    @FXML private Label lblPrecioTotal;

    private TurnoController turnoController;

    private Cliente clienteSeleccionado;

    private final ObservableList<ServicioSelection> listaServiciosSeleccionables =
            FXCollections.observableArrayList();

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm");

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");


    // ==========================================================
    // INITIALIZE
    // ==========================================================

    @FXML
    public void initialize() {

        turnoController = new TurnoController(JpaUtil.getEntityManager());

        cargarServicios();
        configurarEstados();
        configurarDniCliente();
        configurarVeterinarios();
        configurarMascotas();
        configurarFormatoCombos();
        configurarColumnasServicios();
        configurarFechas();
        configurarColumnasTabla();

        // La última columna se estira para llenar el ancho (sin columna vacía)
        tablaSeleccionServicios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaTurnos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        cargarTurnos();
    }


    // ==========================================================
    // CARGA DE DATOS
    // ==========================================================

    private void cargarServicios() {

        listaServiciosSeleccionables.clear();

        for (Servicio servicio : turnoController.listarServicios()) {

            ServicioSelection item = new ServicioSelection(servicio);

            item.selectedProperty().addListener(
                    (obs, oldValue, newValue) -> {

                        evaluarSeleccionGuarderia();
                        recalcularMontoTotal();
                        tablaSeleccionServicios.refresh();
                    }
            );

            listaServiciosSeleccionables.add(item);
        }

        tablaSeleccionServicios.setItems(listaServiciosSeleccionables);
        tablaSeleccionServicios.setEditable(true);
    }


    private void configurarEstados() {

        cmbEstado.setItems(
                FXCollections.observableArrayList(EstadoTurno.PENDIENTE)
        );

        cmbEstado.setValue(EstadoTurno.PENDIENTE);
        cmbEstado.setDisable(true);
    }


    private void configurarDniCliente() {

        txtDniCliente.textProperty().addListener((obs, oldVal, newVal) -> {
            String soloValido = newVal.replaceAll("[^\\d.]", "");
            if (!soloValido.equals(newVal)) {
                txtDniCliente.setText(soloValido);
            }
        });
    }


    @FXML
    public void buscarCliente() {

        String dni = txtDniCliente.getText().trim();

        if (dni.isEmpty()) {
            mostrarAlerta("Atención", "Ingrese el DNI del dueño para buscar.");
            return;
        }

        Optional<Cliente> resultado = turnoController.buscarClientePorDni(dni);

        if (resultado.isEmpty()) {
            mostrarAlerta("Sin resultados", "No se encontró un dueño con DNI " + dni + ".");
            limpiarClienteSeleccionado();
            return;
        }

        clienteSeleccionado = resultado.get();
        lblClienteEncontrado.setText(
                clienteSeleccionado.getNombre()
                        + " " + clienteSeleccionado.getApellido()
        );

        try {

            cmbMascota.setItems(
                    FXCollections.observableArrayList(
                            turnoController.listarMascotasDe(clienteSeleccionado)
                    )
            );

            cmbMascota.setDisable(false);

        } catch (Exception e) {

            mostrarAlerta(
                    "Error",
                    "No se pudieron cargar las mascotas del cliente."
            );
        }

        cmbMascota.setValue(null);
        configurarRestriccionesFechas();
    }


    private void limpiarClienteSeleccionado() {

        clienteSeleccionado = null;
        lblClienteEncontrado.setText("");

        cmbMascota.getItems().clear();
        cmbMascota.setDisable(true);
        cmbMascota.setValue(null);

        configurarRestriccionesFechas();
    }


    private void configurarVeterinarios() {

        cmbVeterinario.setItems(
                FXCollections.observableArrayList(
                        turnoController.listarVeterinarios()
                )
        );
    }


    private void configurarMascotas() {

        cmbMascota.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, mascota) -> {

                    configurarRestriccionesFechas();
                });
    }


    private void configurarFormatoCombos() {

        cmbMascota.setCellFactory(param -> celdaGenerica());
        cmbMascota.setButtonCell(celdaGenerica());

        cmbVeterinario.setCellFactory(param -> celdaGenerica());
        cmbVeterinario.setButtonCell(celdaGenerica());
    }


    private <T> ListCell<T> celdaGenerica() {

        return new ListCell<>() {

            @Override
            protected void updateItem(T item, boolean empty) {

                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        };
    }


    // ==========================================================
    // COLUMNAS DE SERVICIOS
    // ==========================================================

    private void configurarColumnasServicios() {

        colServicioIncluir.setCellValueFactory(
                cellData ->
                        cellData.getValue().selectedProperty()
        );

        colServicioIncluir.setCellFactory(
                CheckBoxTableCell.forTableColumn(colServicioIncluir)
        );

        colServicioNombre.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getServicio()
                                        .getNombre()
                        )
        );

        colServicioDuracion.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue()
                                        .getServicio()
                                        .getDuracionMinutos()
                                        + " min"
                        )
        );

        // El precio estimado lo resuelve el controller: la guardería aplica
        // su regla por cantidad de días y el resto usa su precio base.
        colServicioPrecio.setCellValueFactory(cellData -> {

            Servicio servicio = cellData.getValue().getServicio();

            double precio = turnoController.calcularPrecioEstimado(
                    servicio,
                    ingresoGuarderiaActual(),
                    salidaGuarderiaActual()
            );

            return new SimpleStringProperty(
                    String.format("$ %.2f", precio)
            );
        });
    }


    // ==========================================================
    // FECHAS
    // ==========================================================

    private void configurarFechas() {

        // La fecha del turno es también el ingreso de la guardería.
        dpFecha.valueProperty()
                .addListener((obs, oldValue, newValue) -> {

                    boolean tieneGuarderia =
                            tieneGuarderiaSeleccionada();

                    if (tieneGuarderia) {

                        dpIngresoGuarderia.setValue(newValue);

                    } else {

                        dpIngresoGuarderia.setValue(null);
                    }

                    configurarRestriccionesFechas();
                    recalcularMontoTotal();
                    tablaSeleccionServicios.refresh();
                });


        dpSalidaGuarderia.valueProperty()
                .addListener((obs, oldValue, newValue) -> {

                    recalcularMontoTotal();
                    tablaSeleccionServicios.refresh();

                    configurarRestriccionesFechas();
                });


        txtHoraSalida.textProperty().addListener((obs, oldValue, newValue) -> {
            recalcularMontoTotal();
            tablaSeleccionServicios.refresh();
        });

        configurarRestriccionesFechas();
    }


    private void configurarRestriccionesFechas() {

        LocalDate hoy = LocalDate.now();

        Mascota mascota = cmbMascota.getValue();

        LocalDate nacimiento = mascota != null
                ? mascota.getFechaNacimiento()
                : null;

        LocalDate ingreso = dpIngresoGuarderia.getValue();


        // FECHA DEL TURNO

        dpFecha.setDayCellFactory(picker -> new DateCell() {

            @Override
            public void updateItem(
                    LocalDate date,
                    boolean empty) {

                super.updateItem(date, empty);

                boolean deshabilitar =
                        empty || date.isBefore(hoy);

                if (!deshabilitar &&
                        nacimiento != null &&
                        date.isBefore(nacimiento)) {

                    deshabilitar = true;
                }

                setDisable(deshabilitar);
            }
        });


        // SALIDA GUARDERÍA

        dpSalidaGuarderia.setDayCellFactory(
                picker -> new DateCell() {

                    @Override
                    public void updateItem(
                            LocalDate date,
                            boolean empty) {

                        super.updateItem(date, empty);

                        boolean deshabilitar =
                                empty || date.isBefore(hoy);

                        if (!deshabilitar &&
                                ingreso != null &&
                                date.isBefore(ingreso)) {

                            deshabilitar = true;
                        }

                        if (!deshabilitar &&
                                nacimiento != null &&
                                date.isBefore(nacimiento)) {

                            deshabilitar = true;
                        }

                        setDisable(deshabilitar);
                    }
                }
        );
    }


    // ==========================================================
    // AGENDAR
    // ==========================================================

    @FXML
    public void agendarTurno() {

        if (!validarFormulario()) {
            return;
        }

        try {

            LocalDate fecha = dpFecha.getValue();

            LocalTime hora =
                    LocalTime.parse(
                            txtHora.getText().trim(),
                            timeFormatter
                    );

            LocalDateTime fechaHora =
                    LocalDateTime.of(fecha, hora);


            List<Servicio> servicios =
                    listaServiciosSeleccionables.stream()
                            .filter(ServicioSelection::isSelected)
                            .map(ServicioSelection::getServicio)
                            .toList();


            // Las reglas de negocio las valida y aplica TurnoController.
            turnoController.agendarTurno(
                    fechaHora,
                    cmbVeterinario.getValue(),
                    cmbMascota.getValue(),
                    servicios,
                    ingresoGuarderiaActual(),
                    salidaGuarderiaActual()
            );


            cargarTurnos();
            limpiarCampos();

            mostrarAlerta(
                    "Éxito",
                    "Turno agendado correctamente."
            );

        } catch (Exception e) {

            mostrarAlerta(
                    "Error al agendar",
                    e.getMessage()
            );
        }
    }


    // ==========================================================
    // CONFIRMAR
    // ==========================================================

    @FXML
    public void confirmarTurno() {

        Turno seleccionado =
                tablaTurnos.getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {

            mostrarAlerta(
                    "Atención",
                    "Seleccione un turno de la tabla."
            );

            return;
        }

        try {

            turnoController.confirmarTurno(
                    seleccionado.getIdTurno()
            );

            cargarTurnos();

            mostrarAlerta(
                    "Éxito",
                    "El turno fue confirmado."
            );

        } catch (Exception e) {

            mostrarAlerta(
                    "Error",
                    e.getMessage()
            );
        }
    }


    // ==========================================================
    // CANCELAR
    // ==========================================================

    @FXML
    public void cancelarTurno() {

        Turno seleccionado =
                tablaTurnos.getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {

            mostrarAlerta(
                    "Atención",
                    "Seleccione un turno de la tabla."
            );

            return;
        }


        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Confirma la cancelación del Turno "
                        + seleccionado.getIdTurno()
                        + "?",
                ButtonType.YES,
                ButtonType.NO
        );


        Optional<ButtonType> result =
                confirm.showAndWait();


        if (result.isPresent() &&
                result.get() == ButtonType.YES) {

            try {

                turnoController.cancelarTurno(
                        seleccionado.getIdTurno()
                );

                cargarTurnos();
                limpiarCampos();

                mostrarAlerta(
                        "Éxito",
                        "Turno cancelado correctamente."
                );

            } catch (Exception e) {

                mostrarAlerta(
                        "Error al cancelar",
                        e.getMessage()
                );
            }
        }
    }


    // ==========================================================
    // VALIDACIONES DE INTERFAZ
    // ==========================================================

    private boolean validarFormulario() {

        if (clienteSeleccionado == null ||
                cmbMascota.getValue() == null ||
                cmbVeterinario.getValue() == null ||
                dpFecha.getValue() == null ||
                txtHora.getText().trim().isEmpty()) {

            mostrarAlerta(
                    "Atención",
                    "Complete todos los campos requeridos."
            );

            return false;
        }


        try {

            LocalTime.parse(
                    txtHora.getText().trim(),
                    timeFormatter
            );

        } catch (Exception e) {

            mostrarAlerta(
                    "Atención",
                    "La hora debe tener el formato HH:mm."
            );

            return false;
        }


        boolean hayServicios =
                listaServiciosSeleccionables.stream()
                        .anyMatch(
                                ServicioSelection::isSelected
                        );

        if (!hayServicios) {

            mostrarAlerta(
                    "Atención",
                    "Seleccione al menos un servicio."
            );

            return false;
        }


        if (tieneGuarderiaSeleccionada()) {

            // El ingreso coincide con la fecha/hora del turno; solo se
            // requiere la salida de la guardería.
            if (dpSalidaGuarderia.getValue() == null ||
                    txtHoraSalida.getText().trim().isEmpty()) {

                mostrarAlerta(
                        "Atención",
                        "Complete la salida de la Guardería (fecha y hora HH:mm)."
                );

                return false;
            }

            try {

                LocalTime.parse(txtHoraSalida.getText().trim(), timeFormatter);

            } catch (Exception e) {

                mostrarAlerta(
                        "Atención",
                        "La hora de salida de la Guardería debe tener el formato HH:mm."
                );

                return false;
            }
        }


        return true;
    }


    // ==========================================================
    // GUARDERÍA
    // ==========================================================

    private boolean tieneGuarderiaSeleccionada() {

        return listaServiciosSeleccionables.stream()
                .anyMatch(
                        s -> s.isSelected() &&
                                s.getServicio()
                                        instanceof ServicioGuarderia
                );
    }


    private void evaluarSeleccionGuarderia() {

        boolean tieneGuarderia =
                tieneGuarderiaSeleccionada();

        boxGuarderia.setDisable(!tieneGuarderia);

        if (!tieneGuarderia) {

            dpIngresoGuarderia.setValue(null);
            dpSalidaGuarderia.setValue(null);
            txtHoraSalida.clear();
        }
    }


    // ==========================================================
    // PRECIO - PRESENTACIÓN
    // ==========================================================

    private LocalDateTime ingresoGuarderiaActual() {

        // El ingreso de la guardería coincide con el inicio del turno.
        return combinarFechaHora(
                dpFecha.getValue(),
                txtHora.getText()
        );
    }


    private LocalDateTime salidaGuarderiaActual() {
        return combinarFechaHora(
                dpSalidaGuarderia.getValue(),
                txtHoraSalida.getText()
        );
    }


    private LocalDateTime combinarFechaHora(LocalDate fecha, String hora) {

        if (fecha == null || hora == null || hora.trim().isEmpty()) {
            return null;
        }

        try {

            return LocalDateTime.of(
                    fecha,
                    LocalTime.parse(hora.trim(), timeFormatter)
            );

        } catch (Exception e) {

            return null;
        }
    }


    private void recalcularMontoTotal() {

        List<Servicio> servicios =
                listaServiciosSeleccionables.stream()
                        .filter(ServicioSelection::isSelected)
                        .map(ServicioSelection::getServicio)
                        .toList();


        double total = turnoController.calcularTotalEstimado(
                servicios,
                ingresoGuarderiaActual(),
                salidaGuarderiaActual()
        );


        if (lblPrecioTotal != null) {

            lblPrecioTotal.setText(
                    String.format("$%.2f", total)
            );
        }
    }


    // ==========================================================
    // TABLA
    // ==========================================================

    private void cargarTurnos() {

        List<Turno> lista =
                turnoController.listarTurnos();

        tablaTurnos.setItems(
                FXCollections.observableArrayList(lista)
        );

        tablaTurnos.refresh();
    }


    private void configurarColumnasTabla() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>("idTurno")
        );

        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );


        colFechaHora.setCellValueFactory(cellData -> {

            LocalDateTime fecha =
                    cellData.getValue().getFechaHora();

            return new SimpleStringProperty(
                    fecha != null
                            ? fecha.format(dateTimeFormatter)
                            : ""
            );
        });


        colMascota.setCellValueFactory(cellData -> {

            Mascota mascota =
                    cellData.getValue().getMascota();

            return new SimpleStringProperty(
                    mascota != null
                            ? mascota.getNombre()
                            : ""
            );
        });


        colCliente.setCellValueFactory(cellData -> {

            Mascota mascota =
                    cellData.getValue().getMascota();

            if (mascota != null &&
                    mascota.getCliente() != null) {

                return new SimpleStringProperty(
                        mascota.getCliente().getNombre()
                                + " "
                                + mascota.getCliente().getApellido()
                );
            }

            return new SimpleStringProperty("");
        });


        colVeterinario.setCellValueFactory(cellData -> {

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


        colDuracion.setCellValueFactory(cellData -> {

            int minutos =
                    cellData.getValue()
                            .calcularTiempoTotal();

            return new SimpleStringProperty(
                    minutos + " min"
            );
        });


        colPrecioTotal.setCellValueFactory(cellData -> {

            double total =
                    cellData.getValue()
                            .calcularPrecioTotal();

            return new SimpleStringProperty(
                    String.format("$ %.2f", total)
            );
        });


        colServicios.setCellValueFactory(cellData -> {

            Turno turno = cellData.getValue();

            String servicios = turno.getItems().stream()
                    .map(item -> {

                        String nombre = item.getServicio().getNombre();

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
    // LIMPIAR
    // ==========================================================

    private void limpiarCampos() {

        txtDniCliente.clear();
        limpiarClienteSeleccionado();
        cmbVeterinario.setValue(null);

        dpFecha.setValue(null);

        dpIngresoGuarderia.setValue(null);
        dpSalidaGuarderia.setValue(null);
        txtHoraSalida.clear();

        txtHora.clear();

        cmbEstado.setValue(
                EstadoTurno.PENDIENTE
        );


        listaServiciosSeleccionables
                .forEach(
                        s -> s.setSelected(false)
                );


        recalcularMontoTotal();

        tablaTurnos.getSelectionModel()
                .clearSelection();
    }


    // ==========================================================
    // ALERTAS
    // ==========================================================

    private void mostrarAlerta(
            String titulo,
            String mensaje) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


    // ==========================================================
    // SERVICIO SELECTION (helper de presentación)
    // ==========================================================

    public static class ServicioSelection {

        private final Servicio servicio;

        private final BooleanProperty selected =
                new SimpleBooleanProperty(false);


        public ServicioSelection(Servicio servicio) {
            this.servicio = servicio;
        }


        public Servicio getServicio() {
            return servicio;
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


        @Override
        public String toString() {

            return String.format(
                    "%s - $%.2f (%d min)",
                    servicio.getNombre(),
                    servicio.getPrecio(),
                    servicio.getDuracionMinutos()
            );
        }
    }
}

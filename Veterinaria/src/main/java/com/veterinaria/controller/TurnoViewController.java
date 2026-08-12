package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.*;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.repository.ServicioRepository;
import com.veterinaria.repository.VeterinarioRepository;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

public class TurnoViewController {

    @FXML private ComboBox<Cliente> cmbCliente;
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
    
    @FXML private ListView<ServicioSelection> listServicios;
    @FXML private Label lblPrecioTotal;

    private ServicioRepository servicioRepository;
    private TurnoController turnoController;
    private ClienteRepository clienteRepository;
    private VeterinarioRepository veterinarioRepository;
    private ObservableList<ServicioSelection> listaServiciosSeleccionables = FXCollections.observableArrayList();

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        EntityManager em = JpaUtil.getEntityManager();
        turnoController = new TurnoController(em);
        clienteRepository = new ClienteRepository(em);
        veterinarioRepository = new VeterinarioRepository(em);
        servicioRepository = new ServicioRepository(em);

        // 1. Cargar servicios en el ListView con CheckBoxes (Selección múltiple)
        List<Servicio> serviciosBD = servicioRepository.buscarTodos();
        listaServiciosSeleccionables.clear();

        for (Servicio s : serviciosBD) {
            ServicioSelection item = new ServicioSelection(s);
            // Listener para recalcular el precio total dinámicamente
            item.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> recalcularMontoTotal());
            listaServiciosSeleccionables.add(item);
        }

        listServicios.setItems(listaServiciosSeleccionables);
        listServicios.setCellFactory(CheckBoxListCell.forListView(ServicioSelection::selectedProperty));

        // 2. Columna 'Servicios' en la tabla de turnos
        colServicios.setCellValueFactory(cellData -> {
            Turno t = cellData.getValue();
            if (t.getItems() == null || t.getItems().isEmpty()) {
                return new SimpleStringProperty("-");
            }
            String nombres = t.getItems().stream()
                    .map(item -> item.getServicio().getNombre())
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(nombres);
        });

        // 3. Cargar Estados, Clientes y Veterinarios
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoTurno.values()));
        cmbEstado.setValue(EstadoTurno.PENDIENTE);

        cmbCliente.setItems(FXCollections.observableArrayList(clienteRepository.buscarTodos()));
        cmbVeterinario.setItems(FXCollections.observableArrayList(veterinarioRepository.buscarTodos()));

        // 4. Listener para cargar las Mascotas del Cliente seleccionado
        cmbCliente.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, cliente) -> {
            if (cliente != null) {
                try {
                    EntityManager emLocal = JpaUtil.getEntityManager();
                    Cliente clienteConMascotas = emLocal.find(Cliente.class, cliente.getIdCliente());
                    
                    if (clienteConMascotas != null) {
                        cmbMascota.setItems(FXCollections.observableArrayList(clienteConMascotas.getMascotas()));
                        cmbMascota.setDisable(false);
                    }
                } catch (Exception e) {
                    mostrarAlerta("Error", "No se pudieron cargar las mascotas del cliente: " + e.getMessage());
                }
            } else {
                cmbMascota.getItems().clear();
                cmbMascota.setDisable(true);
            }
            cmbMascota.setValue(null);
        });

        // 5. Restricción en el DatePicker según nacimiento de la mascota
        cmbMascota.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, mascota) -> {
            if (mascota != null && mascota.getFechaNacimiento() != null) {
                dpFecha.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (date.isBefore(mascota.getFechaNacimiento())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                });
            } else {
                dpFecha.setDayCellFactory(null);
            }
        });

        // 6. Formatos, tabla y carga de datos iniciales
        configurarFormatoCombos();
        configurarColumnasTabla();
        cargarTurnos();
    }

    private void configurarFormatoCombos() {
        cmbCliente.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText(empty || cliente == null ? "" : cliente.getDni() + " - " + cliente.getNombre() + " " + cliente.getApellido());
            }
        });
        cmbCliente.setButtonCell(cmbCliente.getCellFactory().call(null));

        cmbMascota.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Mascota mascota, boolean empty) {
                super.updateItem(mascota, empty);
                if (empty || mascota == null) {
                    setText("");
                } else {
                    String razaNombre = mascota.getRaza() != null ? mascota.getRaza().getNombre() : "Sin Raza";
                    setText(mascota.getNombre() + " (" + razaNombre + ")");
                }
            }
        });
        cmbMascota.setButtonCell(cmbMascota.getCellFactory().call(null));

        cmbVeterinario.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Veterinario vet, boolean empty) {
                super.updateItem(vet, empty);
                if (empty || vet == null) {
                    setText("");
                } else {
                    setText("Mat. " + vet.getMatricula() + " - " + vet.getNombre() + " " + vet.getApellido());
                }
            }
        });
        cmbVeterinario.setButtonCell(cmbVeterinario.getCellFactory().call(null));
    }   

    private void cargarTurnos() {
        EntityManager em = JpaUtil.getEntityManager();
        turnoController = new TurnoController(em);
        List<Turno> lista = turnoController.listarTurnos();
        tablaTurnos.setItems(FXCollections.observableArrayList(lista));
        tablaTurnos.refresh();
    }

    @FXML
    public void agendarTurno() {
        if (!validarCampos()) return;
        
        Mascota mascotaSeleccionada = cmbMascota.getValue();
        LocalDate fechaTurno = dpFecha.getValue();

        if (mascotaSeleccionada != null && mascotaSeleccionada.getFechaNacimiento() != null) {
            if (fechaTurno.isBefore(mascotaSeleccionada.getFechaNacimiento())) {
                mostrarAlerta(
                    "Fecha Inválida", 
                    "La fecha del turno (" + fechaTurno.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                    ") no puede ser anterior a la fecha de nacimiento de la mascota (" + 
                    mascotaSeleccionada.getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")."
                );
                return;
            }
        }

        try {
            LocalTime hora = LocalTime.parse(txtHora.getText().trim(), timeFormatter);
            LocalDateTime fechaHora = LocalDateTime.of(fechaTurno, hora);

            Turno nuevo = new Turno(
                fechaHora,
                cmbEstado.getValue(),
                cmbVeterinario.getValue(),
                mascotaSeleccionada
            );

            // 💡 REGLA DE NEGOCIO: Crear un ItemTurno por CADA servicio seleccionado en la lista
            List<ServicioSelection> seleccionados = listaServiciosSeleccionables.stream()
                    .filter(ServicioSelection::isSelected)
                    .toList();

            for (ServicioSelection sel : seleccionados) {
                ItemTurno item = new ItemTurno(sel.getServicio(), nuevo);
                nuevo.agregarItem(item);
            }

            turnoController.agendarTurno(nuevo);

            cargarTurnos();
            limpiarCampos();
            mostrarAlerta("Éxito", "Turno agendado correctamente.");

        } catch (Exception e) {
            mostrarAlerta("Error al agendar", e.getMessage());
        }
    }   

    @FXML
    public void confirmarTurno() {
        Turno seleccionado = tablaTurnos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un turno de la tabla.");
            return;
        }

        try {
            EntityManager em = JpaUtil.getEntityManager();
            em.getTransaction().begin();

            Turno t = em.merge(seleccionado);
            t.confirmar();

            em.getTransaction().commit();

            cargarTurnos();
            mostrarAlerta("Éxito", "El turno pasó a estado CONFIRMADO.");
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    public void atenderTurno() {
        Turno seleccionado = tablaTurnos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un turno de la tabla.");
            return;
        }

        try {
            EntityManager em = JpaUtil.getEntityManager();
            em.getTransaction().begin();

            Turno t = em.merge(seleccionado);
            t.atender();

            em.getTransaction().commit();

            cargarTurnos();
            mostrarAlerta("Éxito", "El turno pasó a estado ATENDIDO.");
        } catch (Exception e) {
            mostrarAlerta("Error", e.getMessage());
        }
    }

    @FXML
    public void cancelarTurno() {
        Turno seleccionado = tablaTurnos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un turno de la tabla para cancelar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "¿Confirma la cancelación del Turno " + seleccionado.getIdTurno() + "?", 
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                turnoController.cancelarTurno(seleccionado.getIdTurno());
                cargarTurnos();
                limpiarCampos();
                mostrarAlerta("Éxito", "Turno cancelado correctamente.");
            } catch (Exception e) {
                mostrarAlerta("Error al cancelar", e.getMessage());
            }
        }
    }

    private boolean validarCampos() {
        boolean hayServiciosSeleccionados = listaServiciosSeleccionables.stream().anyMatch(ServicioSelection::isSelected);

        if (cmbCliente.getValue() == null || cmbMascota.getValue() == null ||
            cmbVeterinario.getValue() == null || dpFecha.getValue() == null || 
            txtHora.getText().trim().isEmpty() || cmbEstado.getValue() == null ||
            !hayServiciosSeleccionados) {

            mostrarAlerta("Atención", "Por favor, complete todos los campos requeridos y seleccione al menos un servicio.");
            return false;
        }

        try {
            LocalTime.parse(txtHora.getText().trim(), timeFormatter);
        } catch (Exception e) {
            mostrarAlerta("Atención", "Formato de hora inválido. Ejemplo: 09:30 o 15:00.");
            return false;
        }

        return true;
    }

    private void limpiarCampos() {
        cmbCliente.setValue(null);
        cmbMascota.setValue(null);
        cmbMascota.setDisable(true);
        cmbVeterinario.setValue(null);
        dpFecha.setValue(null);
        txtHora.clear();
        cmbEstado.setValue(EstadoTurno.PENDIENTE);
        
        // Desmarcar todos los servicios del ListView
        listaServiciosSeleccionables.forEach(s -> s.setSelected(false));
        recalcularMontoTotal();

        tablaTurnos.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private void configurarColumnasTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idTurno"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colFechaHora.setCellValueFactory(cellData -> {
            LocalDateTime fh = cellData.getValue().getFechaHora();
            return new SimpleStringProperty(fh != null ? fh.format(dateTimeFormatter) : "");
        });

        colMascota.setCellValueFactory(cellData -> {
            Mascota m = cellData.getValue().getMascota();
            return new SimpleStringProperty(m != null ? m.getNombre() : "");
        });

        colCliente.setCellValueFactory(cellData -> {
            Mascota m = cellData.getValue().getMascota();
            if (m != null && m.getCliente() != null) {
                return new SimpleStringProperty(m.getCliente().getNombre() + " " + m.getCliente().getApellido());
            }
            return new SimpleStringProperty("");
        });

        colVeterinario.setCellValueFactory(cellData -> {
            Veterinario v = cellData.getValue().getVeterinario();
            if (v != null) {
                return new SimpleStringProperty("Mat. " + v.getMatricula() + " - " + v.getNombre());
            }
            return new SimpleStringProperty("");
        });

        colDuracion.setCellValueFactory(cellData -> {
            int mins = cellData.getValue().calcularTiempoTotal();
            return new SimpleStringProperty(mins + " min");
        });

        colPrecioTotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().calcularPrecioTotal();
            return new SimpleStringProperty(String.format("$ %.2f", total));
        });
    }

    private void recalcularMontoTotal() {
        double total = listaServiciosSeleccionables.stream()
                .filter(ServicioSelection::isSelected)
                .mapToDouble(ServicioSelection::getSubtotal)
                .sum();

        if (lblPrecioTotal != null) {
            lblPrecioTotal.setText(String.format("$%.2f", total));
        }
    }

    public static class ServicioSelection {
        private final Servicio servicio;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        public ServicioSelection(Servicio servicio) {
            this.servicio = servicio;
        }

        public Servicio getServicio() { return servicio; }
        public BooleanProperty selectedProperty() { return selected; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean selected) { this.selected.set(selected); }

        public double getSubtotal() { return servicio.getPrecio(); }

        @Override
        public String toString() {
            return String.format("%s - Subtotal: $%.2f (%d min)", 
                    servicio.getNombre(), servicio.getPrecio(), servicio.getDuracionMinutos());
        }
    }
}
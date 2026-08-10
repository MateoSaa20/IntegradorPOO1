package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.*;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.repository.VeterinarioRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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

    private TurnoController turnoController;
    private ClienteRepository clienteRepository;
    private VeterinarioRepository veterinarioRepository;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    @FXML
public void initialize() {
    EntityManager em = JpaUtil.getEntityManager();
    turnoController = new TurnoController(em);
    clienteRepository = new ClienteRepository(em);
    veterinarioRepository = new VeterinarioRepository(em);

    // 1. Cargar estados
    cmbEstado.setItems(FXCollections.observableArrayList(EstadoTurno.values()));
    cmbEstado.setValue(EstadoTurno.PENDIENTE);
    List<Veterinario> vets = veterinarioRepository.buscarTodos();
    

cmbCliente.setItems(FXCollections.observableArrayList(clienteRepository.buscarTodos()));
cmbVeterinario.setItems(FXCollections.observableArrayList(vets));
    // 2. Cargar Clientes y Veterinarios desde BD
    cmbCliente.setItems(FXCollections.observableArrayList(clienteRepository.buscarTodos()));
    cmbVeterinario.setItems(FXCollections.observableArrayList(veterinarioRepository.buscarTodos()));

    // 3. Listener: al seleccionar un Cliente, cargar de forma segura sus Mascotas
    cmbCliente.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, cliente) -> {
        if (cliente != null) {
            try {
                // Abrimos un EntityManager para inicializar de forma segura la colección de mascotas (evita LazyInitializationException)
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

    // 4. Configurar cómo se muestra el Cliente en el ComboBox (opcional pero recomendado)
    configurarFormatoCombos();

    // 5. Configurar celdas de la tabla y cargar turnos existentes
    configurarColumnasTabla();
    cargarTurnos();
}

/**
 * Define el formato visual en pantalla de los objetos dentro de los desplegables.
 */
private void configurarFormatoCombos() {
    // Formato para Cliente: "DNI - Nombre Apellido"
    cmbCliente.setCellFactory(param -> new ListCell<>() {
        @Override
        protected void updateItem(Cliente cliente, boolean empty) {
            super.updateItem(cliente, empty);
            setText(empty || cliente == null ? "" : cliente.getDni() + " - " + cliente.getNombre() + " " + cliente.getApellido());
        }
    });
    cmbCliente.setButtonCell(cmbCliente.getCellFactory().call(null));

    // Formato para Mascota: "Nombre (Raza)"
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

    // 💡 AGREGAR ESTE BLOQUE: Formato para Veterinario "Mat. [Matricula] - [Nombre]"
    // Formato para Veterinario: "Mat. [Matricula] - [Nombre] [Apellido]"
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
        tablaTurnos.setItems(FXCollections.observableArrayList(turnoController.listarTurnos()));
    }

    @FXML
    public void agendarTurno() {
        if (!validarCampos()) return;

        try {
            LocalTime hora = LocalTime.parse(txtHora.getText().trim(), timeFormatter);
            LocalDateTime fechaHora = LocalDateTime.of(dpFecha.getValue(), hora);

            Turno nuevo = new Turno(
                fechaHora,
                cmbEstado.getValue(),
                cmbVeterinario.getValue(),
                cmbMascota.getValue()
            );

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
        if (cmbCliente.getValue() == null || cmbMascota.getValue() == null ||
            cmbVeterinario.getValue() == null || dpFecha.getValue() == null || 
            txtHora.getText().trim().isEmpty() || cmbEstado.getValue() == null) {

            mostrarAlerta("Atención", "Por favor, complete todos los campos requeridos.");
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
    // ID Turno y Estado
    colId.setCellValueFactory(new PropertyValueFactory<>("idTurno"));
    colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

    // Fecha y Hora formateada (ejemplo: 10/08/2026 09:30)
    colFechaHora.setCellValueFactory(cellData -> {
        LocalDateTime fh = cellData.getValue().getFechaHora();
        return new SimpleStringProperty(fh != null ? fh.format(dateTimeFormatter) : "");
    });

    // Nombre de la Mascota
    colMascota.setCellValueFactory(cellData -> {
        Mascota m = cellData.getValue().getMascota();
        return new SimpleStringProperty(m != null ? m.getNombre() : "");
    });

    // Nombre y Apellido del Cliente (dueño de la mascota)
    colCliente.setCellValueFactory(cellData -> {
        Mascota m = cellData.getValue().getMascota();
        if (m != null && m.getCliente() != null) {
            return new SimpleStringProperty(m.getCliente().getNombre() + " " + m.getCliente().getApellido());
        }
        return new SimpleStringProperty("");
    });

    // Datos del Veterinario (Matrícula y Nombre)
    colVeterinario.setCellValueFactory(cellData -> {
        Veterinario v = cellData.getValue().getVeterinario();
        if (v != null) {
            return new SimpleStringProperty("Mat. " + v.getMatricula() + " - " + v.getNombre());
        }
        return new SimpleStringProperty("");
    });

    // Tiempo total sumado de sus ItemTurno
    colDuracion.setCellValueFactory(cellData -> {
        int mins = cellData.getValue().calcularTiempoTotal();
        return new SimpleStringProperty(mins + " min");
    });

    // Precio total sumado de sus ItemTurno
    colPrecioTotal.setCellValueFactory(cellData -> {
        double total = cellData.getValue().calcularPrecioTotal();
        return new SimpleStringProperty(String.format("$ %.2f", total));
    });
}
}
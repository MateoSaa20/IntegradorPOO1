package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.*;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.repository.EspecieRepository;
import com.veterinaria.repository.MascotaRepository;
import com.veterinaria.repository.RazaRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClienteMascotaViewController {

    @FXML private TextField txtNombre, txtApellido, txtDni, txtTelefono;
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colDni, colNombre, colApellido, colTelefono;

    @FXML private TextField txtMascotaNombre;
    @FXML private ComboBox<Sexo> cmbSexo;
    @FXML private ComboBox<Especie> cmbEspecie; // <-- Agregado
    @FXML private ComboBox<Raza> cmbRaza;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TableView<Mascota> tablaMascotas;
    @FXML private TableColumn<Mascota, Long> colFicha;
    @FXML private TableColumn<Mascota, String> colMascotaNombre;
    @FXML private TableColumn<Mascota, Raza> colRaza;
    @FXML private TableColumn<Mascota, Sexo> colSexo;
    @FXML private TableColumn<Mascota, LocalDate> colFechaNac;

    private ClienteController clienteController;
    private RazaRepository razaRepository;
    private EspecieRepository especieRepository;
    private MascotaRepository mascotaRepository;
    private ClienteRepository clienteRepository;

    private List<Raza> todasLasRazas;
    private Cliente clienteEnEdicion = null; // Para mantener el cliente seleccionado al editar
    private Mascota mascotaEnEdicion = null; // Para mantener la mascota seleccionada al editar
   @FXML
public void initialize() {
    EntityManager em = JpaUtil.getEntityManager();
    clienteController = new ClienteController(em);
    clienteRepository = new ClienteRepository(em);
    especieRepository = new EspecieRepository(em);
    razaRepository = new RazaRepository(em);

   // =========================================================
    // 1. CARGAR Y CONFIGURAR EL COMBOBOX DE ESPECIES
    // =========================================================
    List<Especie> especies = especieRepository.buscarTodos();
    cmbEspecie.setItems(FXCollections.observableArrayList(especies));

    // CellFactory: Formato para la lista desplegable de Especie
    cmbEspecie.setCellFactory(param -> new ListCell<Especie>() {
        @Override
        protected void updateItem(Especie item, boolean empty) {
            super.updateItem(item, empty);
            // ✅ Corregido a getNombreEspecie()
            setText(empty || item == null ? "" : item.getNombre()); 
        }
    });

    // ButtonCell: Formato para el texto seleccionado dentro del ComboBox
    cmbEspecie.setButtonCell(new ListCell<Especie>() {
        @Override
        protected void updateItem(Especie item, boolean empty) {
            super.updateItem(item, empty);
            // ✅ Corregido a getNombreEspecie()
            setText(empty || item == null ? "" : item.getNombre()); 
        }
    });

   // =========================================================
// 2. LISTENER DE ESPECIE PARA FILTRAR RAZAS
// =========================================================
cmbEspecie.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, especieSeleccionada) -> {
    if (especieSeleccionada != null) {
        // 💡 Pasamos la entidad Especie entera en lugar de solo su ID
        List<Raza> razas = razaRepository.buscarPorEspecie(especieSeleccionada);
        
        cmbRaza.setItems(FXCollections.observableArrayList(razas));
        cmbRaza.setDisable(false); // Habilita el ComboBox
    } else {
        cmbRaza.getItems().clear();
        cmbRaza.setDisable(true);
    }
    cmbRaza.setValue(null);
});
    // CellFactory y ButtonCell para el ComboBox de Raza
    cmbRaza.setCellFactory(param -> new ListCell<Raza>() {
        @Override
        protected void updateItem(Raza item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? "" : item.getNombre());
        }
    });
    cmbRaza.setButtonCell(new ListCell<Raza>() {
        @Override
        protected void updateItem(Raza item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? "" : item.getNombre());
        }
    });

    // =========================================================
    // 3. COMBO DE SEXO Y DEMÁS CONFIGURACIONES
    // =========================================================
    cmbSexo.setItems(FXCollections.observableArrayList(Sexo.values()));

        // Configuración de columnas
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        colFicha.setCellValueFactory(new PropertyValueFactory<>("numeroFicha"));
        colMascotaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRaza.setCellValueFactory(new PropertyValueFactory<>("raza"));
        colSexo.setCellValueFactory(new PropertyValueFactory<>("sexo"));
        colFechaNac.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));

        // Validaciones numéricas en tiempo real
        txtDni.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) txtDni.setText(newVal.replaceAll("[^\\d]", ""));
        });
        txtTelefono.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) txtTelefono.setText(newVal.replaceAll("[^\\d]", ""));
        });
        txtNombre.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*")) {
            txtNombre.setText(newValue.replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]", ""));
        }
    });

    // Restringir Apellido a solo letras, espacios y caracteres acentuados
    txtApellido.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*")) {
            txtApellido.setText(newValue.replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]", ""));
        }
    });

    // Restringir Nombre de Mascota a solo letras, espacios y caracteres acentuados
    txtMascotaNombre.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*")) {
            txtMascotaNombre.setText(newValue.replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]", ""));
        }
    });

        cargarClientes();

        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, cliente) -> {
            if (cliente != null) {
                this.clienteEnEdicion = cliente; // Guardamos el cliente seleccionado
                txtNombre.setText(cliente.getNombre());
                txtApellido.setText(cliente.getApellido());
                txtDni.setText(String.valueOf(cliente.getDni()));
                txtTelefono.setText(String.valueOf(cliente.getTelefono()));
                cargarMascotasDelCliente(cliente);
            }
        });

        tablaMascotas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, mascota) -> {
    if (mascota != null) {
        this.mascotaEnEdicion = mascota;
        txtMascotaNombre.setText(mascota.getNombre());
        cmbSexo.setValue(mascota.getSexo());
        
        if (mascota.getRaza() != null) {
            cmbEspecie.setValue(mascota.getRaza().getEspecie());
            cmbRaza.setValue(mascota.getRaza());
        }
        
        dpFechaNacimiento.setValue(mascota.getFechaNacimiento());
    }
});

    }

    private void cargarClientes() {
        tablaClientes.setItems(FXCollections.observableArrayList(clienteController.listarTodos()));
    }

    private void cargarMascotasDelCliente(Cliente cliente) {
        tablaMascotas.setItems(FXCollections.observableArrayList(cliente.getMascotas()));
    }

    @FXML
    public void guardarCliente() {
        if (!validarCamposCliente()) return;

        try {
            Cliente nuevo = new Cliente(
                txtNombre.getText().trim(),
                txtApellido.getText().trim(),
                Integer.parseInt(txtDni.getText().trim()),
                Integer.parseInt(txtTelefono.getText().trim())
            );
            clienteController.registrarCliente(nuevo);
            cargarClientes();
            limpiarCamposCliente();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo guardar el cliente: " + e.getMessage());
        }
    }

    @FXML
    public void editarCliente() {
       
        if (clienteEnEdicion == null) {
            mostrarAlerta("Atención", "Seleccione un cliente de la tabla para editar.");
            return;
        }

        if (!validarCamposCliente()) return;

        try {
            clienteEnEdicion.setNombre(txtNombre.getText().trim());
            clienteEnEdicion.setApellido(txtApellido.getText().trim());
            clienteEnEdicion.setDni(Integer.parseInt(txtDni.getText().trim()));
            clienteEnEdicion.setTelefono(Integer.parseInt (txtTelefono.getText().trim()));

            clienteController.actualizar(clienteEnEdicion);

            tablaClientes.refresh(); // Refresca la tabla para mostrar los cambios
            
            limpiarCamposCliente();
            
            mostrarAlerta("Éxito", "Cliente actualizado correctamente.");

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo actualizar el cliente: " + e.getMessage());
        }
    }

    @FXML
    public void eliminarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un cliente de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar a " + seleccionado.getNombre() + "?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                clienteController.eliminar(seleccionado.getIdCliente());
                cargarClientes();
                tablaMascotas.getItems().clear();
                limpiarCamposCliente();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo eliminar el cliente: " + e.getMessage());
            }
        }
    }

    @FXML
    public void guardarMascota() {
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un cliente de la tabla antes de agregar una mascota.");
            return;
        }

        if (txtMascotaNombre.getText().trim().isEmpty() || 
            cmbSexo.getValue() == null || 
            cmbEspecie.getValue() == null ||
            cmbRaza.getValue() == null || 
            dpFechaNacimiento.getValue() == null) {
            
            mostrarAlerta("Atención", "Todos los campos de la mascota son obligatorios.");
            return;
        }

        try {
            Mascota nueva = new Mascota();
            nueva.setNombre(txtMascotaNombre.getText().trim());
            nueva.setSexo(cmbSexo.getValue());
            nueva.setRaza(cmbRaza.getValue());
            nueva.setCliente(clienteSeleccionado);
            nueva.setFechaNacimiento(dpFechaNacimiento.getValue());

            clienteController.agregarMascotaACliente(clienteSeleccionado.getIdCliente(), nueva);

            cargarClientes();
            cargarMascotasDelCliente(clienteSeleccionado);
            limpiarCamposMascota();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo agregar la mascota: " + e.getMessage());
        }
    }

    @FXML
public void eliminarMascota() {
    Mascota seleccionada = tablaMascotas.getSelectionModel().getSelectedItem();
    Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();

    if (seleccionada == null) {
        mostrarAlerta("Atención", "Seleccione una mascota para eliminar.");
        return;
    }

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "¿Desea eliminar a la mascota " + seleccionada.getNombre() + "?", 
            ButtonType.YES, ButtonType.NO);
    Optional<ButtonType> result = confirm.showAndWait();

    if (result.isPresent() && result.get() == ButtonType.YES) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // 💡 Re-asociamos la entidad al EntityManager antes de borrarla
            Mascota aEliminar = em.merge(seleccionada);
            em.remove(aEliminar);

            em.getTransaction().commit();

            // Si la lista del cliente en memoria tiene la mascota, la removemos también localmente
            if (clienteSeleccionado != null) {
                clienteSeleccionado.getMascotas().remove(seleccionada);
                cargarMascotasDelCliente(clienteSeleccionado);
            }

            limpiarCamposMascota();
            mostrarAlerta("Éxito", "Mascota eliminada correctamente.");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            mostrarAlerta("Error", "No se pudo eliminar la mascota: " + e.getMessage());
        }
    }
}
    @FXML
public void editarMascota() {
    if (mascotaEnEdicion == null) {
        mostrarAlerta("Atención", "Seleccione una mascota de la tabla para editar.");
        return;
    }

    if (txtMascotaNombre.getText().trim().isEmpty() || 
        cmbSexo.getValue() == null || 
        cmbEspecie.getValue() == null ||
        cmbRaza.getValue() == null || 
        dpFechaNacimiento.getValue() == null) {
        
        mostrarAlerta("Atención", "Todos los campos de la mascota son obligatorios.");
        return;
    }

    Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();

    EntityManager em = JpaUtil.getEntityManager();
    try {
        em.getTransaction().begin();
        
        mascotaEnEdicion.setNombre(txtMascotaNombre.getText().trim());
        mascotaEnEdicion.setSexo(cmbSexo.getValue());
        mascotaEnEdicion.setRaza(cmbRaza.getValue());
        mascotaEnEdicion.setFechaNacimiento(dpFechaNacimiento.getValue());

        em.merge(mascotaEnEdicion);
        em.getTransaction().commit();

        tablaMascotas.refresh(); // Refresca la tabla para mostrar los cambios

        limpiarCamposMascota();


        mostrarAlerta("Éxito", "Mascota actualizada correctamente.");

    } catch (Exception e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        mostrarAlerta("Error", "No se pudo actualizar la mascota: " + e.getMessage());
    }
}

  

    private boolean validarCamposCliente() {
        if (txtNombre.getText().trim().isEmpty() || 
            txtApellido.getText().trim().isEmpty() || 
            txtDni.getText().trim().isEmpty()) {
            
            mostrarAlerta("Atención", "Por favor, complete al menos Nombre, Apellido y DNI.");
            return false;
        }

        if (txtDni.getText().trim().length() < 7) {
            mostrarAlerta("Atención", "El DNI ingresado no es válido (debe tener al menos 7 dígitos).");
            return false;
        }

        return true;
    }

    private void limpiarCamposCliente() {
        txtNombre.clear();
        txtApellido.clear();
        txtDni.clear();
        txtTelefono.clear();
        this.clienteEnEdicion = null; // Limpiamos el cliente en edición
        tablaClientes.getSelectionModel().clearSelection();
    }

    private void limpiarCamposMascota() {
        txtMascotaNombre.clear();
        cmbSexo.setValue(null);
        cmbEspecie.setValue(null);
        cmbRaza.setValue(null);
        cmbRaza.setDisable(true);
        dpFechaNacimiento.setValue(null);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
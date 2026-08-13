package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.*;
import com.veterinaria.repository.EspecieRepository;
import com.veterinaria.repository.RazaRepository;
import com.veterinaria.util.TextoUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    private Cliente clienteEnEdicion = null; // Para mantener el cliente seleccionado al editar
    private Mascota mascotaEnEdicion = null; // Para mantener la mascota seleccionada al editar
   @FXML
public void initialize() {
    EntityManager em = JpaUtil.getEntityManager();
    clienteController = new ClienteController(em);
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

        // La última columna se estira para llenar el ancho (sin columna vacía)
        tablaClientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaMascotas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Validaciones numéricas en tiempo real
        txtDni.textProperty().addListener((obs, oldVal, newVal) -> {
            String soloDigitos = newVal.replaceAll("[^\\d]", "");
            if (soloDigitos.length() > 8) soloDigitos = soloDigitos.substring(0, 8);
            if (!soloDigitos.equals(newVal)) txtDni.setText(soloDigitos);
        });
        txtTelefono.textProperty().addListener((obs, oldVal, newVal) -> {
            String soloDigitos = newVal.replaceAll("[^\\d]", "");
            if (soloDigitos.length() > 12) soloDigitos = soloDigitos.substring(0, 12);
            if (!soloDigitos.equals(newVal)) txtTelefono.setText(soloDigitos);
        });
        // Filtro en vivo: solo letras, espacios y caracteres acentuados.
        // La capitalización se aplica al perder el foco (así no se traga
        // los espacios mientras se escribe la siguiente palabra).
        txtNombre.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches(TextoUtil.SOLO_LETRAS)) {
                txtNombre.setText(newValue.replaceAll(TextoUtil.NO_LETRAS, ""));
            }
        });
        txtNombre.focusedProperty().addListener((obs, oldValue, focused) -> {
            if (!focused) txtNombre.setText(TextoUtil.capitalizar(txtNombre.getText()));
        });

    // Restringir Apellido a solo letras, espacios y caracteres acentuados
    txtApellido.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches(TextoUtil.SOLO_LETRAS)) {
            txtApellido.setText(newValue.replaceAll(TextoUtil.NO_LETRAS, ""));
        }
    });
    txtApellido.focusedProperty().addListener((obs, oldValue, focused) -> {
        if (!focused) txtApellido.setText(TextoUtil.capitalizar(txtApellido.getText()));
    });

    // Restringir Nombre de Mascota a solo letras, espacios y caracteres acentuados
    txtMascotaNombre.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches(TextoUtil.SOLO_LETRAS)) {
            txtMascotaNombre.setText(newValue.replaceAll(TextoUtil.NO_LETRAS, ""));
        }
    });
    txtMascotaNombre.focusedProperty().addListener((obs, oldValue, focused) -> {
        if (!focused) txtMascotaNombre.setText(TextoUtil.capitalizar(txtMascotaNombre.getText()));
    });

    // La fecha de nacimiento no puede ser posterior a hoy
    dpFechaNacimiento.setDayCellFactory(param -> new DateCell() {
        @Override
        public void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            setDisable(!empty && item.isAfter(LocalDate.now()));
        }
    });

        cargarClientes();

        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, cliente) -> {
            if (cliente != null) {
                this.clienteEnEdicion = cliente; // Guardamos el cliente seleccionado
                txtNombre.setText(cliente.getNombre());
                txtApellido.setText(cliente.getApellido());
                txtDni.setText(cliente.getDni());
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
                txtDni.getText().trim(),
                txtTelefono.getText().trim()
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
            clienteEnEdicion.setDni(txtDni.getText().trim());
            clienteEnEdicion.setTelefono(txtTelefono.getText().trim());

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

        if (!validarCamposMascota()) return;

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

    if (!validarCamposMascota()) return;

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
            txtDni.getText().trim().isEmpty() ||
            txtTelefono.getText().trim().isEmpty()) {
            
            mostrarAlerta("Atención", "Por favor, complete al menos Nombre, Apellido, DNI y Teléfono.");
            return false;
        }

        if (txtDni.getText().trim().length() > 8) {
            mostrarAlerta("Atención", "El DNI ingresado no es válido (debe tener hasta 8 dígitos).");
            return false;
        }

        if (txtTelefono.getText().trim().length() > 12) {
            mostrarAlerta("Atención", "El teléfono no es válido (debe tener hasta 12 dígitos).");
            return false;
        }

        return true;
    }

    private boolean validarCamposMascota() {
        if (txtMascotaNombre.getText().trim().isEmpty() ||
                cmbSexo.getValue() == null ||
                cmbEspecie.getValue() == null ||
                cmbRaza.getValue() == null ||
                dpFechaNacimiento.getValue() == null) {

            mostrarAlerta("Atención", "Todos los campos de la mascota son obligatorios.");
            return false;
        }

        if (dpFechaNacimiento.getValue().isAfter(LocalDate.now())) {
            mostrarAlerta("Atención", "La fecha de nacimiento no puede ser posterior a la fecha actual.");
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
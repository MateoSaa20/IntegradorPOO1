package com.veterinaria.controller;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.model.*;
import com.veterinaria.repository.MascotaRepository;
import com.veterinaria.repository.RazaRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Optional;

public class ClienteMascotaViewController {

    @FXML private TextField txtNombre, txtApellido, txtDni, txtTelefono;
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colDni, colNombre, colApellido, colTelefono;

    @FXML private TextField txtMascotaNombre;
    @FXML private ComboBox<Sexo> cmbSexo;
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
    private MascotaRepository mascotaRepository;

    @FXML
    public void initialize() {
        EntityManager em = JpaUtil.getEntityManager();
        clienteController = new ClienteController(em);
        razaRepository = new RazaRepository(em);
        mascotaRepository = new MascotaRepository(em);

        cmbSexo.setItems(FXCollections.observableArrayList(Sexo.values()));
        cmbRaza.setItems(FXCollections.observableArrayList(razaRepository.buscarTodos()));

        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        colFicha.setCellValueFactory(new PropertyValueFactory<>("numeroFicha"));
        colMascotaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRaza.setCellValueFactory(new PropertyValueFactory<>("raza"));
        colSexo.setCellValueFactory(new PropertyValueFactory<>("sexo"));
        colFechaNac.setCellValueFactory(new PropertyValueFactory<>("fechaNacimiento"));

        cargarClientes();

        // Al seleccionar un cliente, se cargan sus campos arriba y sus mascotas abajo
        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, cliente) -> {
            if (cliente != null) {
                txtNombre.setText(cliente.getNombre());
                txtApellido.setText(cliente.getApellido());
                txtDni.setText(cliente.getDni());
                txtTelefono.setText(cliente.getTelefono());
                cargarMascotasDelCliente(cliente);
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
        try {
            Cliente nuevo = new Cliente(
                txtNombre.getText(),
                txtApellido.getText(),
                txtDni.getText(),
                txtTelefono.getText()
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
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Seleccione un cliente de la tabla para editar.");
            return;
        }

        try {
            seleccionado.setNombre(txtNombre.getText());
            seleccionado.setApellido(txtApellido.getText());
            seleccionado.setDni(txtDni.getText());
            seleccionado.setTelefono(txtTelefono.getText());

            clienteController.actualizar(seleccionado);
            cargarClientes();
            limpiarCamposCliente();
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

        if (cmbRaza.getValue() == null) {
            mostrarAlerta("Atención", "Seleccione una raza.");
            return;
        }

        try {
            Mascota nueva = new Mascota();
            nueva.setNombre(txtMascotaNombre.getText());
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

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar a la mascota " + seleccionada.getNombre() + "?", ButtonType.YES, ButtonType.NO);
    Optional<ButtonType> result = confirm.showAndWait();

    if (result.isPresent() && result.get() == ButtonType.YES) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            mascotaRepository.eliminar(seleccionada);
            em.getTransaction().commit();

            if (clienteSeleccionado != null) {
                cargarMascotasDelCliente(clienteSeleccionado);
            }
            limpiarCamposMascota();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            mostrarAlerta("Error", "No se pudo eliminar la mascota: " + e.getMessage());
        }
    }
}

    private void limpiarCamposCliente() {
        txtNombre.clear();
        txtApellido.clear();
        txtDni.clear();
        txtTelefono.clear();
    }

    private void limpiarCamposMascota() {
        txtMascotaNombre.clear();
        cmbSexo.setValue(null);
        cmbRaza.setValue(null);
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
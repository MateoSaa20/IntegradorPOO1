package com.veterinaria.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainViewController {

    @FXML 
    private BorderPane mainLayout;
    @FXML
    private StackPane contentArea;

    @FXML
    public void mostrarClientes() {
        cargarVista("/fxml/ClienteMascotaView.fxml");
    }

    @FXML
    public void mostrarTurnos() {
        cargarVista("/fxml/TurnosView.fxml");
    }

    @FXML
    public void mostrarHistorial() {
        cargarVista("/fxml/HistorialView.fxml");
    }

    private void cargarVista(String fxmlPath) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(vista);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista " + fxmlPath + ": " + e.getMessage());
        }
    }
}
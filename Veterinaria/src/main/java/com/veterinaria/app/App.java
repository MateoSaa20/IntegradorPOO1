package com.veterinaria.app;

import com.veterinaria.config.DataInitializer;
import com.veterinaria.config.JpaUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        DataInitializer.cargarDatosIniciales(JpaUtil.getEntityManager());
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1024, 680);

        primaryStage.setTitle("Sistema de Gestión Veterinaria");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        DataInitializer.cargarServiciosIniciales();
        launch(args);
    }
}
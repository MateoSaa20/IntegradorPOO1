package com.veterinaria.app;

import com.veterinaria.config.JpaUtil;
import com.veterinaria.controller.*;
import com.veterinaria.model.*;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            System.out.println("=== INICIANDO PRUEBA CONFORME AL DIAGRAMA UML ===");

            // 1. Instanciar Controladores
            ClienteController clienteController = new ClienteController(em);
            VeterinarioController vetController = new VeterinarioController(em);
            TurnoController turnoController = new TurnoController(em);
            AtencionController atencionController = new AtencionController(em);

            // 2. Crear Datos Maestros (Usando ServicioConsulta por ser Servicio una clase abstracta)
            Especie perro = new Especie("Canino");
            Raza labradormix = new Raza("Labrador", perro);
            Especialidad clinica = new Especialidad("Clínica General", "Atención médica general para mascotas, incluyendo revisiones, diagnósticos y tratamientos básicos.");
            
            // Instanciamos ServicioConsulta (subclase concreta de Servicio)
            ServicioConsulta consultaGeneral = new ServicioConsulta("Consulta General", 5000.0, 30);

            // Persistir maestros
            em.getTransaction().begin();
            em.persist(perro);
            em.persist(labradormix);
            em.persist(clinica);
            em.persist(consultaGeneral);
            em.getTransaction().commit();

            // 3. Registrar Cliente y Mascota
            Cliente cliente = new Cliente("Juan", "Pérez", "123456", "3764000000");
            Mascota mascota = new Mascota("Panchito", LocalDate.of(2023, 2, 11), Sexo.MACHO, labradormix);
            cliente.agregarMascota(mascota);
            
            clienteController.registrarCliente(cliente);
            System.out.println("✔ Cliente y Mascota registrados exitosamente.");

            // 4. Registrar Veterinario y Especialidad
            Veterinario vet = new Veterinario("Dra. Ana", "Gómez", "MP-999");
            vetController.registrarVeterinario(vet);
            vetController.agregarEspecialidad(vet.getIdVeterinario(), clinica);
            System.out.println("✔ Veterinario registrado con su Especialidad.");

            // 5. Agendar Turno asociando el ItemTurno al ServicioConsulta
            Turno turno = new Turno(LocalDateTime.now().plusDays(2), EstadoTurno.PENDIENTE, vet, mascota);
            
            // ItemTurno captura precioAlMomento y tiempoAlMomento del servicio como especifica el UML
            ItemTurno itemTurno = new ItemTurno(consultaGeneral);
            turno.agregarItem(itemTurno);

            turnoController.agendarTurno(turno);
            System.out.println("✔ Turno agendado con ID: " + turno.getIdTurno() + " | Estado: " + turno.getEstado());

            // 6. Registrar Atención Médica (DetalleConsulta -> DetalleAtencion -> ItemTurno)
            DetalleConsulta detalle = new DetalleConsulta("Revisión general en consultorio", "Paciente en buen estado general");
            
            atencionController.registrarDetalleAtencion(turno.getIdTurno(), itemTurno.getIdItemTurno(), detalle);
            System.out.println("✔ Atención registrada exitosamente. Nuevo estado del turno: " + turno.getEstado());
System.out.println("\n==========================================");
            System.out.println("  VERIFICACIÓN DE DATOS EN LA BASE DE DATOS");
            System.out.println("==========================================");

            // 1. Consultar Clientes y sus Mascotas asociadas
            System.out.println("\n📋 [CLIENTES Y MASCOTAS]");
            clienteController.listarTodos().forEach(c -> {
                System.out.println(" • Cliente: " + c.getNombre() + " " + c.getApellido() + " (DNI: " + c.getDni() + ")");
                c.getMascotas().forEach(m -> 
                    System.out.println("   └─ Mascota: " + m.getNombre() + " | Raza: " + m.getRaza().getNombre() + " | Sexo: " + m.getSexo())
                );
            });

            // 2. Consultar Turno cargado
            // 2. Consultar Turno cargado directamente con JPA
            System.out.println("\n📅 [TURNO Y ESTADO]");
            Turno tGuardado = em.find(Turno.class, turno.getIdTurno());
            if (tGuardado != null) {
                System.out.println(" • Turno ID: " + tGuardado.getIdTurno());
                System.out.println(" • Estado Actual: " + tGuardado.getEstado());
                System.out.println(" • Fecha/Hora: " + tGuardado.getFechaHora());
                System.out.println(" • Veterinario: " + tGuardado.getVeterinario().getNombre() + " " + tGuardado.getVeterinario().getApellido());
            }

            System.out.println("\n==========================================");
            System.out.println("  ¡TODAS LAS PRUEBAS PASARON CORRECTAMENTE!");
            System.out.println("==========================================\n");
            


        } catch (Exception e) {
            System.err.println("❌ ERROR DURANTE LA PRUEBA:");
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
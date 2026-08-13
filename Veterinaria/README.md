# Sistema Veterinaria

Sistema de gestión veterinaria de escritorio que administra clientes, mascotas, turnos, atenciones, historial clínico y control de vacunaciones.

## Tecnologías

- Java 25
- JavaFX 25 (interfaz gráfica)
- Hibernate ORM 7 / JPA 3.2
- H2 Database (base de datos embebida en `database/veterinaria.mv.db`)
- Maven
- JUnit 5

## Arquitectura y flujo general

La aplicación sigue una arquitectura en capas. Cada pantalla se arma de la siguiente forma:

```
Vista (FXML)
   ↓
*ViewController   → maneja la interfaz (combos, tablas, formularios)
   ↓
*Controller       → capa delgada que orquesta, expone operaciones a la vista
   ↓
*Service          → reglas de negocio + transacciones (Transaccion)
   ↓
*Repository       → consultas JPA/JPQL
   ↓
JPA / Hibernate → H2 Database
```

- `model/`: entidades JPA. Además de los datos, concentran parte de las reglas de negocio (por ejemplo `Turno.validarDisponibilidad`, `Turno.confirmar`, `Servicio.calcularSubtotal`).
- `config/`:
  - `JpaUtil`: crea el `EntityManagerFactory` a partir de la unidad de persistencia `VeterinariaPU`.
  - `DataInitializer`: carga datos iniciales de ejemplo. Es idempotente: cada sección solo inserta si la tabla correspondiente está vacía, por lo que puede ejecutarse en cada inicio sin duplicar datos.
  - `H2Console`: levanta la consola web de H2 en `http://localhost:8082`.
- `service/Transaccion`: encapsula `begin/commit/rollback` para que las transacciones no se repitan en cada operación.
- `util/TextoUtil`: normaliza textos (nombres a formato título).

## Flujo de inicio

1. `App.main` crea un `EntityManager` y ejecuta `DataInitializer.cargarDatosIniciales(em)`.
2. `launch(args)` arranca JavaFX y `App.start` carga `MainView.fxml` (ventana principal con menú lateral).
3. El `MainViewController` recibe los clicks del menú y carga la vista de cada módulo en el área central (`contentArea`) usando `FXMLLoader`.

Módulos disponibles: Clientes y Mascotas, Turnos, Atención, Historial, Vacunaciones, Servicios, Tipos de Vacuna y Veterinarios.

## Ciclo de vida de un Turno (flujo completo)

### 1. Agendar (`TurnoView`)

El usuario busca al dueño por DNI, elige mascota, veterinario, fecha/hora y selecciona uno o más servicios (con checkboxes). Si elige Guardería, debe completar la salida (fecha y hora).

`TurnoService.agendarTurno` valida y persiste:

- La fecha/hora del turno debe ser **futura** (`validarFechaFutura`).
- Si incluye guardería, el ingreso no puede ser posterior al inicio del turno y debe indicarse la salida.
- Para cada servicio se construye un `ItemTurno` (o `ItemGuarderia` para guardería):
  - **Guardería**: valida el rango de fechas y que no supere el **cupo máximo** de animales en simultáneo.
  - **Vacuna**: no se puede repetir mientras esté dentro de su **periodicidad** en meses (`validarPeriodicidadVacuna`).
- El veterinario no debe tener **turnos superpuestos** ese día (la guardería no ocupa al veterinario).
- La mascota tampoco puede tener otro turno en el mismo horario (aquí la guardería sí ocupa).
- Al persistir se congelan en cada `ItemTurno` el **precio** y la **duración** del servicio al momento del agendado (histórico).

### 2. Estados del turno

```
PENDIENTE → CONFIRMADO → ATENDIDO
     └──────── CANCELADO (con al menos 24 h de anticipación)
```

- `confirmar()`: solo desde `PENDIENTE`.
- `atender()`: solo desde `CONFIRMADO`.
- `cancelar()`: solo desde `PENDIENTE` o `CONFIRMADO` y con 24 h de anticipación.

### 3. Atención (`AtencionView`)

- Solo se listan turnos **CONFIRMADO**.
- Por cada servicio del turno se carga un `DetalleAtencion`:
  - Consulta → `DetalleConsulta` (observaciones, diagnóstico y tratamientos).
  - Vacunación → `DetalleVacunacion` (laboratorio/marca y observaciones de la dosis).
  - Peluquería/Guardería → `DetalleAtencion` simple.
- Se valida que el tipo de detalle corresponda con el servicio del item.
- Al confirmar la atención, el turno pasa a `ATENDIDO` y queda registrado en el historial.

### 4. Historial (`HistorialView`)

- Se busca al dueño por DNI y se listan sus mascotas.
- Para cada mascota se muestran las atenciones realizadas (turnos `ATENDIDO`) y las vacunas aplicadas.

### 5. Control de vacunaciones (`VacunacionView`)

- `EstadoVacuna` (DTO de solo lectura) resume cada vacuna cíclica: fecha de la última aplicación y próxima dosis.
- Calcula el estado: **Al día**, **Por vencer** (menos de 30 días) o **Vencida**.
- `VacunacionService.listarVacunasEnAlerta` genera alertas globales ordenando primero las vencidas.
- `registrarVacunacion` permite cargar una vacunación aplicada directamente (crea el turno `ATENDIDO` correspondiente) validando la periodicidad.

## Reglas de negocio principales

- Un turno no puede agendarse en un día/hora pasado.
- No se permiten turnos superpuestos para el mismo veterinario ni para la misma mascota.
- La guardería se cobra por cantidad de días y tiene un cupo máximo de animales en simultáneo.
- Una vacuna no puede repetirse dentro de su ventana de periodicidad.
- Precio y duración se congelan al momento de agendar (el servicio puede cambiar después sin afectar turnos previos).
- La cancelación requiere al menos 24 horas de anticipación.

## Cómo ejecutar y testear

```bash
# Compilar y ejecutar la aplicación
mvn clean javafx:run

# Ejecutar los tests
mvn test

# Consola web de H2 (opcional): http://localhost:8082
# DB embebida: database/veterinaria.mv.db
```

La primera ejecución carga datos de ejemplo (especies, razas, veterinarios, servicios, clientes, mascotas, turnos, vacunas y una guardería completa para el día siguiente) para poder navegar y probar todos los módulos de inmediato.

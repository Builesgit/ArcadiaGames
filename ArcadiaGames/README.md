# Arcadia Games - Sistema de Login & Animación 3D

Este proyecto es una aplicación de Android desarrollada en **Kotlin** que combina una experiencia visual dinámica con un sistema de autenticación funcional conectado a una base de datos **MySQL** mediante **servicios web (PHP)**.

## Características del Proyecto

### 1. Animación de Transición (Splash Screen)
* **Efecto Flip 3D**: Al iniciar, la aplicación presenta una animación de rotación en el eje Y que simula el giro de una pantalla para revelar el login.
* **Carga de Multimedia**: Se utiliza la librería **Glide** para cargar un logo animado (GIF) de manera fluida y optimizada.
* **Transiciones Suaves**: Implementación de `AccelerateDecelerateInterpolator` para lograr un movimiento natural en el cambio de pantallas.

### 2. Sistema de Autenticación Funcional
* **Validación de Datos**: Comprobación de campos vacíos antes del envío.
* **Comunicación Asíncrona**: Uso de la librería **Volley** para realizar peticiones `POST` al servidor sin bloquear la interfaz de usuario.
* **Gestión de Sesión**: Paso de datos entre actividades (`Intent Extras`) para mostrar el nombre del usuario logueado en la pantalla de inicio.

## IMPORTANTE: Configuración del Servidor (XAMPP)

Para que el sistema de validación funcione, es **estrictamente necesario** configurar el entorno de servidor local.

> ### Carpeta `arcadia_games_db`
> Este repositorio incluye una carpeta llamada `arcadia_games_db` que contiene los archivos necesarios para la validación.
> 
> **Instrucciones de instalación:**
> 1. Copia la carpeta completa **`arcadia_games_db`**.
> 2. Pégala dentro del directorio **`htdocs`** de tu instalación de XAMPP (por defecto: `C:/xampp/htdocs/`).
> 3. Sin estos archivos en el servidor Apache, la aplicación no podrá realizar la validación.

## Preparación de la Base de Datos

1. **Abrir XAMPP**: Inicia los módulos de **Apache** y **MySQL**.
2. **Crear BD**: Desde `phpMyAdmin`, crea una base de datos llamada `arcadia_games_db`.
3. **Importar Datos**: Utiliza el archivo `.sql` incluido en la carpeta `arcadia_games_db` para crear la tabla `usuarios`.

### Scripts PHP Incluidos:
* **`config.php`**: Define los parámetros de conexión al servidor MySQL.
* **`validar_usuario.php`**: Realiza la consulta SQL utilizando **Sentencias Preparadas** para garantizar la seguridad contra inyecciones SQL.

## Configuración en Android Studio

### 1. Dependencias (build.gradle.kts)
Para que la comunicación con la base de datos funcione, debes asegurarte de tener añadida la librería **Volley** en tu archivo `build.gradle.kts` (nivel de módulo: app):

```kotlin
dependencies {
    // Librería para peticiones HTTP
    implementation("com.android.volley:volley:1.2.1")
    
    // Librería para carga de GIFs y animaciones
    implementation("com.github.bumptech.glide:glide:4.16.0")
}

# Arcadia Games

Este proyecto es una aplicación de Android desarrollada en **Kotlin** que combina una experiencia visual dinámica con un sistema de autenticación funcional conectado a una base de datos **MySQL** mediante **servicios web (PHP)**.

## Organización del Repositorio

Para mejorar la limpieza del código fuente de Android, se ha reestructurado el repositorio de la siguiente manera:
* **Carpeta Raíz**: Ahora contiene el archivo `README.md`, la carpeta de la base de datos para un acceso rápido y claramente el proyecto de Arcadia Games
* **Carpeta de Base de Datos**: Los archivos PHP y SQL se encuentran fuera del proyecto de Android para evitar confusiones con el código de la aplicación.

## Características del Proyecto

### 1. Animación de Transición (Splash Screen)
* **Efecto Flip 3D**: Al iniciar, la aplicación presenta una animación de rotación en el eje Y que simula el giro de una pantalla para revelar el login.
* **Estado Actual (Imagen Estática)**: La carga del GIF mediante **Glide** se ha desactivado temporalmente para optimizar las pruebas. Actualmente se utiliza una imagen de fondo fija (`fondo_con_logo`).
* **Documentación**: Toda la lógica del GIF permanece comentada y explicada en el código de `MainActivity.kt` para su posterior activación.
* **Transiciones Suaves**: Uso de `AccelerateDecelerateInterpolator` para un movimiento natural.

### 2. Sistema de Autenticación Funcional
* **Validación de Datos**: Control de campos vacíos antes del envío.
* **Comunicación Asíncrona**: Uso de la librería **Volley** para realizar peticiones `POST` al servidor.
* **Código Comentado**: Se han añadido comentarios detallados en cada bloque de código (View Binding, Animaciones y Red) para facilitar la comprensión del equipo.

---

## CONFIGURACIÓN OBLIGATORIA DEL SERVIDOR (XAMPP)

Para que el sistema de login funcione, cada colaborador debe configurar su entorno local:

### 1. Ubicación de los archivos PHP
1. Localiza la carpeta de la base de datos en la raíz del repositorio.
2. Copia su contenido.
3. Ve a tu directorio de XAMPP: `C:\xampp\htdocs\`.
4. Crea una carpeta llamada **`arcadia_games_db`**.
5. Pega dentro los archivos `config.php` y `validar_usuario.php`.

> **Nota**: La aplicación apunta a `http://10.0.2.2/arcadia_games_db/`. Esta IP es necesaria para que el emulador reconozca el localhost de tu PC.

### 2. Preparación de la Base de Datos
1. Inicia **Apache** y **MySQL** en XAMPP.
2. Accede a `phpMyAdmin` y crea una base de datos llamada `arcadia_games_db`.
3. Importa el archivo `.sql` incluido para generar la tabla de usuarios.

---

## Configuración Técnica (Android Studio)

### Dependencias (build.gradle)
Asegúrate de tener estas librerías configuradas:

```kotlin
dependencies {

    // Librería para peticiones HTTP (Volley)
    implementation("com.android.volley:volley:1.2.1")
    
    // Librería para imágenes y GIFs (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
}
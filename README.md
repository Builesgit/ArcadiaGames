# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa moderna diseñada para la gestión integral de una tienda de videojuegos. Destaca por su interfaz de usuario "Dark Gamer Premium", su navegación fluida y una integración robusta con los servicios de Firebase para una experiencia en tiempo real y segura.

---

## Funcionalidades Principales

### 1. Gestión de Roles y Seguridad
* **Autenticación:** Sistema gestionado mediante **Firebase Authentication**.
* **Jerarquía de Usuarios:**
    * **Admin Jefe:** Único perfil con capacidad para generar códigos de invitación y gestionar al equipo de administración.
    * **Admin Subordinado:** Acceso a la gestión de inventario y reportes de stock.
    * **Clientes:** Acceso al catálogo, filtrado de juegos por plataforma y gestión de perfil.
* **Sistema de Invitación:** Generación de códigos aleatorios con **validez temporal (5 min)** para ascender usuarios de forma segura.

### 2. Experiencia de Usuario (UX/UI Avanzada)
* **View Binding:** Implementación de vinculación de vistas para un código más limpio y eficiente.
* **Navigation Rail Dinámico:** Menú lateral ergonómico que maximiza el espacio vertical, separando la navegación de las acciones de soporte.
* **Mando de Control Central:** Botón flotante ("Mando Púrpura") que actúa como disparador global para la navegación lateral.
* **Tema Dark Premium:** Estética de alta fidelidad con paleta `#202020` y acentos neón.

### 3. Catálogo y Multimedia
* **Optimización con Glide:** Carga eficiente de imágenes mediante `AppGlideModule` (`MyAppGlideModule`), asegurando un rendimiento fluido y gestión de caché inteligente.
* **Carrusel Infinito:** Uso de `CarouselLayoutManager` con scroll circular para destacar las novedades del catálogo.

---

## Arquitectura del Proyecto

### Lógica (Kotlin - `app/src/main/java/...`)

| Componente | Responsabilidad |
| :--- | :--- |
| **`MainActivity`** | Gestiona el flujo de inicio de sesión y la carga dinámica de layouts. |
| **`HomeActivity`** | Centro neurálgico que controla el Navigation Rail y los fragmentos principales. |
| **`MyAppGlideModule`** | Configuración centralizada de Glide para la optimización de recursos gráficos. |
| **`FirebaseInventoryManager`** | Clase Singleton que centraliza las operaciones CRUD con Realtime Database. |

---

## Stack Tecnológico

* **Lenguaje:** Kotlin 1.9+.
* **Plataforma:** Android (minSdk 27 - targetSdk 34).
* **Servicios Cloud:** Firebase Auth, Realtime Database y Firebase Storage.
* **Librerías:** Glide 4.16, Material Design 3 (M3).

---

## Notas de la Versión (v1.7.0)
* **Eliminación de App Check:** Desvinculación completa de los proveedores de App Check tanto en el código fuente como en las dependencias de Gradle para agilizar el desarrollo y las pruebas en emuladores.
* **Implementación de MyAppGlideModule:** Creación del módulo generado de Glide para optimizar la carga de imágenes, eliminando advertencias en el Logcat y mejorando la fluidez visual de la app.

---
© 2026 ArcadiaGames - Desarrollado para la gestión moderna de videojuegos.
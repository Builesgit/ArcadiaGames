# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa moderna diseñada para la gestión integral de una tienda de videojuegos. Destaca por su interfaz de usuario "Dark Gamer Premium", su navegación fluida y una integración robusta con los servicios de Firebase para una experiencia en tiempo real y segura.

---

## Funcionalidades Principales

### 1. Gestión de Roles y Seguridad
* **Autenticación:** Sistema gestionado mediante **Firebase Authentication**.
* **Jerarquía de Usuarios:**
    * **Admin Jefe:** Único perfil con capacidad para gestionar al equipo y generar códigos.
    * **Admin Subordinado:** Acceso a inventario, incidencias e historial.
    * **Clientes:** Acceso al catálogo, historial de compras y soporte técnico.

### 2. Experiencia de Usuario (UX/UI Avanzada)
* **Perfil Dinámico:** Interfaz de usuario que se adapta en tiempo real según el rol del usuario (Admin/Cliente), mostrando herramientas específicas para cada jerarquía.
* **Navigation Rail:** Menú lateral ergonómico que optimiza el espacio de trabajo.
* **Mando de Control:** Acceso global a la navegación mediante el botón flotante temático "Mando Púrpura".
* **Tema Dark Premium:** Paleta de colores `#202020` con acentos neón para una estética gamer profesional.

### 3. Catálogo y Multimedia
* **Optimización Glide:** Gestión avanzada de imágenes para garantizar fluidez y ahorro de datos.
* **Filtrado Inteligente:** Búsqueda y filtrado dinámico por plataformas (PC, PS, Xbox, Nintendo).

---

## Arquitectura del Proyecto

### Lógica (Kotlin - `app/src/main/java/...`)

| Componente | Responsabilidad |
| :--- | :--- |
| **`FragmentPerfil`** | Controla la lógica de visibilidad y nomenclatura de botones según el rol del usuario. |
| **`HomeActivity`** | Gestiona la navegación principal y el inflado de fragmentos. |
| **`MyAppGlideModule`** | Módulo de configuración para la optimización multimedia. |

### Diseño (XML - `app/src/main/res/layout/`)

| Archivo | Descripción |
| :--- | :--- |
| **`activity_perfil.xml`** | Layout de perfil con secciones modulares para administradores y usuarios estándar. |
| **`activity_register.xml`** | Interfaz de registro de nuevos usuarios. |

---

## Stack Tecnológico

* **Lenguaje:** Kotlin 1.9+.
* **Plataforma:** Android (minSdk 27).
* **Servicios Cloud:** Firebase Auth, Realtime Database, Firebase Storage.
* **Librerías:** Glide 4.16, Material Design 3.

---

## Notas de la Versión (v1.8.0)
* **Rediseño de Interfaz de Perfil:** Se ha simplificado la vista de usuario para mejorar la claridad visual, manteniendo los botones de "Subir Juego" y "Mis Intercambios" comentados en el código para futuras implementaciones.
* **Actualización de Nomenclatura:**
    * **Admin:** El botón "Ver Reportes" ha sido renombrado a **"Historial"** para una mejor identificación semántica.
    * **Usuario:** El botón "Mis Juegos" ha sido renombrado a **"Mis Compras"** para reflejar su función actual.
* **Optimización de FragmentPerfil:** Mejora en la lógica de escucha en tiempo real de Firebase para actualizar la interfaz del perfil instantáneamente al cambiar el rol del usuario.

---
© 2026 ArcadiaGames - Desarrollado para la gestión moderna de videojuegos.
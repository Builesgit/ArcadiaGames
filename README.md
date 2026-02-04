# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa moderna diseñada para la gestión integral de una tienda de videojuegos. Destaca por su interfaz de usuario "Dark Gamer Premium", su navegación fluida y una integración robusta con los servicios de Firebase para una experiencia en tiempo real y altamente segura.

---

## Funcionalidades Principales

### 1. Autenticación y Jerarquía de Roles
* **Seguridad:** Sistema de autenticación gestionado mediante **Firebase Authentication**.
* **Protección Anti-Bot:** Implementación de **Firebase App Check** para asegurar que solo la aplicación oficial pueda interactuar con los datos.
* **Jerarquía de Administradores:**
    * **Admin Jefe:** Único perfil con capacidad para generar códigos de invitación y gestionar al equipo.
    * **Admin Subordinado:** Acceso a gestión de inventario y reportes.
    * **Clientes:** Acceso al catálogo, filtrado de juegos y gestión de perfil personal.
* **Sistema de Invitación:** Generación de códigos aleatorios con **validez de 5 minutos** para ascender usuarios de forma segura.

### 2. Experiencia de Usuario (UX/UI Avanzada)
* **Home Dinámico:** Rediseño del flujo de inicio con gestión de visibilidad por capas (Layouts), integrando el sistema de Login y Navegación principal en una estructura de **View Binding** eficiente.
* **Navigation Rail Dinámico:** Menú lateral ergonómico que maximiza el espacio vertical, separando los ítems de navegación de las acciones de soporte.
* **Mando de Control Central:** Integración de un botón flotante ("Mando Púrpura") que actúa como disparador global para abrir y cerrar el menú lateral.
* **Cierre Inteligente:** Lógica de cierre por proximidad, ocultando automáticamente el menú al interactuar con el contenido principal.
* **Tema Oscuro Gamer:** Estética de alta fidelidad con paleta `#202020` y acentos neón.

### 3. Pantalla de Inicio y Ayuda
* **Carrusel Infinito:** Implementación de `CarouselLayoutManager` con lógica de scroll circular y ajuste de imágenes mediante **Glide (centerCrop)** para un acabado profesional.
* **Guía de Arcadia:** Sección de ayuda integrada con diseño centrado y tipografía *italic-bold*, ofreciendo instrucciones claras sobre el uso de la app.

### 4. Catálogo Inteligente
* **Doble Filtrado:** Localización mediante `SearchBar` y filtrado por plataforma (PC, PS, Xbox, Nintendo) con chips dinámicos.
* **Gestión de Stock Visual:** Indicadores de disponibilidad mediante colores dinámicos: **Verde** para disponible y **Rojo** para agotado.

---

## Guía de Configuración para Desarrolladores (App Check)

Para que el equipo de desarrollo pueda probar la aplicación sin bloqueos de seguridad de Firebase, cada integrante debe seguir estos pasos en su entorno local:

1. **Obtener Huella Digital (SHA-256):**
   * Abrir la terminal en Android Studio y ejecutar: `./gradlew signingReport`.
   * Copiar el código **SHA-256** generado y registrarlo en la Consola de Firebase (*Configuración del proyecto > General > Añadir huella digital*).

2. **Obtener Token de Depuración (Debug Secret):**
   * Ejecutar la app en el emulador o dispositivo físico.
   * En el **Logcat**, filtrar por la etiqueta: `AppCheckDebugCritical`.
   * Copiar el código alfanumérico (UUID) que aparece y añadirlo en: *Firebase Console > App Check > Apps > Gestionar tokens de depuración*.

3. **Habilitar BuildConfig:**
   * El proyecto requiere que `buildConfig = true` esté presente en el archivo `build.gradle` para reconocer el entorno de depuración.

---

## Arquitectura del Proyecto

### Lógica (Kotlin - `app/src/main/java/...`)

| Componente | Responsabilidad |
| :--- | :--- |
| **`MainActivity`** | Punto de entrada. Inicializa App Check y gestiona el flujo de Login por capas de visibilidad. |
| **`HomeActivity`** | Centro neurálgico de la app. Gestiona el Navigation Rail y el contenido principal. |
| **`RegisterActivity`** | Gestiona el flujo de creación de cuentas (anteriormente vinculado a `crearcuenta_layout`). |
| **`JuegoAdapter`** | Renderizado del catálogo con optimización de imágenes (Glide) y gestión de clics. |

### Diseño (XML - `app/src/main/res/layout/`)

* **`activity_main.xml`**: Layout base que contiene las pantallas de inicio y login alternables.
* **`activity_register.xml`**: Layout optimizado para el registro de nuevos usuarios.
* **`rail_header.xml` / `rail_footer.xml`**: Componentes modulares para la navegación lateral.
* **`item_novedades_carrousel.xml`**: Diseño de tarjetas de carrusel con ajuste de imagen a pantalla completa.

---

## Stack Tecnológico

* **Lenguaje:** Kotlin 1.9+.
* **Plataforma:** Android (minSdk 24).
* **Servicios Cloud:** Firebase Auth, Realtime Database (con reglas `auth.token.appcheck`), Firebase Storage y App Check.
* **Diseño:** Material Design 3 (M3).

---

## Notas de Versión
* **v1.6.0 (Versión Actual):**
    * Implementación de **Firebase App Check** (Seguridad de nivel profesional).
    * Rediseño de **MainActivity** para carga dinámica de layouts (Inicio/Login).
    * Refactorización de archivos XML: `crearcuenta_layout` -> `activity_register`.
    * Optimización de imágenes en el carrusel con **Glide centerCrop**.
    * Actualización de reglas de seguridad en Realtime Database.

---
© 2026 ArcadiaGames - Desarrollado para la gestión moderna de videojuegos.
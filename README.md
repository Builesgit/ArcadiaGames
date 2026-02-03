# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa moderna diseñada para la gestión integral de una tienda de videojuegos. Destaca por su interfaz de usuario "Dark Gamer Premium", su navegación fluida y una integración robusta con los servicios de Firebase para una experiencia en tiempo real.

---

## Funcionalidades Principales

### 1. Autenticación y Jerarquía de Roles
* **Seguridad:** Sistema de autenticación gestionado mediante **Firebase Authentication**.
* **Jerarquía de Administradores:**
    * **Admin Jefe:** Único perfil con capacidad para generar códigos de invitación y gestionar al equipo de administración.
    * **Admin Subordinado:** Acceso a gestión de inventario y reportes.
    * **Clientes:** Acceso al catálogo, filtrado de juegos y gestión de perfil personal.
* **Sistema de Invitación:** Generación de códigos aleatorios con **validez de 5 minutos** para ascender usuarios de forma segura.

### 2. Experiencia de Usuario (UX/UI Avanzada)
* **Navigation Rail Dinámico:** Menú lateral ergonómico que maximiza el espacio vertical, separando los ítems de navegación de las acciones de soporte.
* **Mando de Control Central:** Integración de un botón flotante ("Mando Púrpura") que actúa como disparador global para abrir y cerrar el menú lateral, mejorando la inmersión temática.
* **Cierre Inteligente:** El menú lateral implementa una lógica de cierre por proximidad, ocultándose automáticamente al interactuar con el contenido principal de la pantalla.
* **Tema Oscuro:** Estética de alta fidelidad con paleta `#202020` y acentos neón.
* **Pop-up de Detalle:** Visualización de información detallada de juegos mediante un cuadro de diálogo centrado y estilizado.

### 3. Pantalla de Inicio y Ayuda
* **Carrusel Infinito:** Implementación de `CarouselLayoutManager` con lógica de scroll circular para destacar novedades.
* **Guía de Arcadia:** Nueva sección de ayuda integrada con diseño centrado y tipografía *italic-bold*, ofreciendo instrucciones claras sobre el uso del catálogo y filtros.

### 4. Catálogo Inteligente
* **Doble Filtrado:** Localización mediante `SearchBar` y filtrado por plataforma (PC, PS, Xbox, Nintendo) con chips dinámicos.
* **Gestión de Stock Visual:** Indicadores de disponibilidad mediante colores dinámicos: **Verde** para productos en stock y **Rojo** para agotados.

---

## Arquitectura del Proyecto

### Lógica (Kotlin - `app/src/main/java/...`)

| Componente | Responsabilidad |
| :--- | :--- |
| **`HomeActivity`** | Gestiona la visibilidad del Rail mediante el mando púrpura y el cierre al tocar fuera. |
| **`FragmentGuiaUso`** | Implementa la interfaz de ayuda con scroll y textos centrados para mejorar la legibilidad. |
| **`JuegoAdapter`** | Adaptador dual que incluye soporte para clics opcionales y apertura de detalles. |
| **`FirebaseInventoryManager`** | Clase Singleton que centraliza operaciones con la nube. |

### Diseño (XML - `app/src/main/res/layout/`)

* **`rail_header.xml`**: Define el espacio superior de la barra lateral para un espaciado limpio con el logo.
* **`rail_footer.xml`**: Contenedor anclado al fondo para el acceso rápido a la información de uso ("i").
* **`fragment_guia_uso.xml`**: Layout optimizado con `ConstraintLayout` y `NestedScrollView` para visualización de textos centrados.

---

## Stack Tecnológico

* **Lenguaje:** Kotlin 1.9+.
* **Plataforma:** Android (minSdk 24).
* **Servicios Cloud:** Firebase Auth, Realtime Database y Firebase Storage.
* **Diseño:** Material Design 3 (M3).

---

## Notas de Versión
* **v1.5.0 (Versión Actual):** * Rediseño estructural del **Navigation Rail** para optimizar el espacio de los fragmentos.
    * Implementación del **Mando Púrpura** como controlador externo de navegación.
    * Lógica de **cierre automático** de la barra lateral al detectar interacción fuera del menú.
    * Nueva interfaz de **Guía de Arcadia** con alineación centrada y diseño optimizado para lectura rápida.
    * Corrección de superposiciones entre el Header, el menú de navegación y el Footer de información.

---
© 2026 ArcadiaGames - Desarrollado para la gestión moderna de videojuegos.
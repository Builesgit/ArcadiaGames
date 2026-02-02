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

### 2. Experiencia de Usuario (UX/UI)
* **Navigation Rail:** Menú lateral ergonómico que maximiza el espacio vertical.
* **Tema Oscuro:** Estética de alta fidelidad con paleta `#202020` y acentos neón.
* **Pop-up de Detalle (Novedad):** Visualización de información detallada de juegos mediante un cuadro de diálogo centrado y estilizado, permitiendo ver descripción, plataforma y stock sin abandonar la lista principal.

### 3. Pantalla de Inicio (Home)
* **Carrusel Infinito:** Implementación de `CarouselLayoutManager` con lógica de scroll circular para destacar novedades.
* **Sincronización Híbrida:** Renderizado de contenido estático y dinámico desde la nube simultáneamente.

### 4. Catálogo Inteligente
* **Doble Filtrado:** Localización mediante `SearchBar` y filtrado por plataforma (PC, PS, Xbox, Nintendo) con chips dinámicos.
* **Gestión de Stock Visual:** Indicadores de disponibilidad mediante colores dinámicos: **Verde** para productos en stock y **Rojo** para agotados (Stock 0).

### 5. Gestión de Inventario (Backend)
* **Editor Avanzado:** `AnadirProductoActivity` con validación de campos, captura de imagen mediante cámara frontal y previsualización.
* **Stock Inteligente:** Migración del campo stock a tipo numérico (`Int`), permitiendo una gestión automática y precisa de las unidades disponibles.
* **Eliminación Granular:** Sistema de gestión que permite eliminar unidades individuales o lotes completos de productos agrupados por nombre.

---

## Arquitectura del Proyecto

### Lógica (Kotlin - `app/src/main/java/...`)

| Componente | Responsabilidad |
| :--- | :--- |
| **`JuegoAdapter`** | Adaptador dual (Lista/Carrusel) que ahora incluye soporte para clics opcionales y apertura de detalles. |
| **`GestionarAdapter`** | Gestión de inventario con agrupación por nombre y visualización de stock total acumulado. |
| **`Juego.kt`** | Data Class optimizada con campo `stock` de tipo `Int` para operaciones aritméticas. |
| **`FirebaseInventoryManager`** | Clase Singleton que centraliza operaciones con Realtime Database y Firebase Storage. |

### Diseño (XML - `app/src/main/res/layout/`)

* **`dialog_detalle_juego.xml`**: Interfaz del pop-up centrado con diseño de recuadro negro redondeado y botones de acción.
* **`item_juego_catalogo_u.xml`**: Tarjeta de producto optimizada para el catálogo del usuario final.
* **`fondo_popup_negro.xml`**: Drawable personalizado que define la estética de los cuadros de diálogo flotantes.

---

## Stack Tecnológico

* **Lenguaje:** Kotlin 1.9+.
* **Plataforma:** Android (minSdk 24).
* **Servicios Cloud:** Firebase Auth, Realtime Database y Firebase Storage.
* **Librerías Externas:** Glide para el procesamiento y caché de imágenes en tiempo real.

---

## Notas de Versión
* **v1.4.0:** Implementado pop-up de detalle centrado, migración de stock a `Int` y lógica de visualización de stock por colores.
* **v1.3.5:** Añadida funcionalidad de captura de imagen con cámara frontal y previsualización en el editor.
* **v1.3.0:** Añadida jerarquía de **Admin Jefe**, generación de códigos de 5 min y actualización de perfiles en tiempo real.
* **v1.2.5:** Rediseño del panel de gestión con contenedores en gris oscuro y visualización de equipo por correo electrónico.
* **v1.1.5:** Integrada la lógica de formateo automático de precios `%.2f €`.
* **v1.0.0:** Lanzamiento inicial con sistema de roles y carrusel infinito.

---
© 2026 ArcadiaGames - Desarrollado para la gestión moderna de videojuegos.
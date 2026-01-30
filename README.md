# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa moderna diseñada para la gestión integral de una tienda de videojuegos. Destaca por su interfaz de usuario "Dark Gamer Premium", su navegación fluida y una integración robusta con los servicios de Firebase para una experiencia en tiempo real.

---

## Funcionalidades Principales

### 1. Autenticación y Jerarquía de Roles (Novedad)
* **Seguridad:** Sistema de autenticación gestionado mediante **Firebase Authentication**.
* **Jerarquía de Administradores:**
    * **Admin Jefe:** Único perfil con capacidad para generar códigos de invitación y gestionar al equipo de administración (Degradar/Ascender).
    * **Admin Subordinado:** Acceso a gestión de inventario y reportes, pero sin permisos de gestión de equipo.
    * **Clientes:** Acceso al catálogo, filtrado de juegos y gestión de perfil personal.
* **Sistema de Invitación:** Generación de códigos aleatorios con **validez de 5 minutos** para ascender usuarios a administradores de forma segura y efímera.

### 2. Experiencia de Usuario (UX/UI)
* **Navigation Rail:** Menú lateral ergonómico que maximiza el espacio vertical en pantallas modernas.
* **Tema Oscuro:** Estética de alta fidelidad con paleta `#202020` y acentos vibrantes en amarillo y verde neón.
* **Actualización en Tiempo Real:** Perfiles reactivos que cambian su interfaz instantáneamente al detectar un cambio de rol en la base de datos (mediante `ValueEventListener`) sin necesidad de reiniciar la app.

### 3. Pantalla de Inicio (Home)
* **Carrusel Infinito:** Implementación de `CarouselLayoutManager` con lógica de scroll circular para destacar novedades.
* **Sincronización Híbrida:** Capacidad de renderizar contenido promocional estático mientras sincroniza datos dinámicos desde la nube.

### 4. Catálogo Inteligente
* **Doble Filtrado:** Localización mediante `SearchBar` y filtrado por plataforma (PC, PS, Xbox, Nintendo) con chips centrados dinámicamente.
* **Conectividad:** Uso de listeners de Firebase para reflejar cambios en el stock o precios al instante.

### 5. Gestión de Inventario (Backend)
* **Editor Avanzado:** `AnadirProductoActivity` con validación de campos y previsualización de imagen.
* **Formateo de Precios:** Lógica de validación que asegura que los precios siempre se almacenen con dos decimales y el símbolo correspondiente (ej: `59.00 €`).

---

## Arquitectura del Proyecto

### Lógica (Kotlin - `app/src/main/java/...`)

| Componente | Responsabilidad |
| :--- | :--- |
| **`FragmentPerfil`** | Interfaz reactiva que escucha cambios de rol y gestiona el canje de códigos de invitación. |
| **`GestionAdminsActivity`** | Panel exclusivo del Jefe para generar invitaciones y listar administradores en un recuadro oscuro. |
| **`AdminAdapter`** | Adaptador especializado para visualizar el equipo de gestión mediante su correo electrónico. |
| **`Usuario.kt`** | Data Class optimizada para jerarquía con campos `rol`, `esJefe` y `usuario` (email). |
| **`FirebaseInventoryManager`** | Clase Singleton que centraliza las operaciones de red con RTDB y Storage. |

### Diseño (XML - `app/src/main/res/layout/`)

* **`activity_gestion_admins.xml`**: Panel administrativo con diseño de recuadro gris oscuro para separar la lista de equipo.
* **`item_admin.xml`**: Tarjeta de usuario optimizada para mostrar el correo electrónico y botón de degradación de rango.
* **`activity_register.xml`**: Interfaz de registro mejorada (anteriormente `crearcuenta_layout.xml`).

---

## Stack Tecnológico

* **Lenguaje:** Kotlin 1.9+.
* **Plataforma:** Android (minSdk 24).
* **Servicios Cloud:** Firebase Auth, Realtime Database y Firebase Storage.
* **Seguridad:** Reglas JSON personalizadas para restringir acceso a códigos de admin solo al perfil `esJefe`.

---

## Notas de Versión
* **v1.3.0:** Añadida jerarquía de **Admin Jefe**, generación de códigos de 5 min y actualización de perfiles en tiempo real.
* **v1.2.5:** Rediseño del panel de gestión con contenedores en gris oscuro y visualización de equipo por correo electrónico.
* **v1.2.0:** Implementado el centrado dinámico de chips de plataforma en el catálogo.
* **v1.1.5:** Integrada la lógica de formateo automático de precios `%.2f €`.
* **v1.0.0:** Lanzamiento inicial con sistema de roles y carrusel infinito.

---
© 2026 ArcadiaGames - Desarrollado para la gestión moderna de videojuegos.
# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa moderna diseñada para la gestión integral de una tienda de videojuegos. Destaca por su interfaz de usuario "Dark Gamer Premium", su navegación fluida y una integración robusta con los servicios de Firebase para una experiencia en tiempo real.

---

## Funcionalidades Principales

### 1. Autenticación y Jerarquía de Roles (Actualizado)
* **Seguridad:** Sistema de autenticación gestionado mediante **Firebase Authentication**.
* **Jerarquía de Administradores:**
    * **Admin Jefe:** Único perfil con capacidad para generar códigos de invitación y gestionar al equipo de administración (Degradar/Ascender).
    * **Admin Subordinado:** Acceso a gestión de inventario y edición de productos.
    * **Clientes:** Acceso al catálogo, filtrado de juegos y gestión de perfil personal.
* **Sistema de Invitación:** Generación de códigos aleatorios con **validez de 5 minutos**. Al canjearse, el usuario asciende a Admin y el código se destruye automáticamente.

### 2. Experiencia de Usuario (UX/UI)
* **Perfil Reactivo:** Implementación de `addValueEventListener` en el perfil para conmutar la interfaz entre "Modo Usuario" y "Modo Administrador" instantáneamente al detectar cambios en la base de datos.
* **Navigation Rail:** Menú lateral ergonómico para una navegación optimizada en dispositivos modernos.
* **Tema Oscuro Premium:** Paleta de colores `#202020` con acentos en amarillo y diseño de tarjetas con transparencias.

### 3. Pantalla de Inicio y Catálogo
* **Carrusel Infinito:** Destacados dinámicos mediante `CarouselLayoutManager`.
* **Doble Filtrado:** Localización por nombre (`SearchBar`) y categorías por plataforma (PC, PS, Xbox, Nintendo) con chips centrados dinámicamente.
* **Sincronización RTDB:** Los cambios en el catálogo se reflejan en todos los dispositivos conectados sin necesidad de recargar.

### 4. Gestión de Inventario y Equipo
* **Panel de Gestión de Admins:** Recuadro gris oscuro estilizado para la visualización clara de administradores subordinados identificados por su correo electrónico.
* **Backend de Productos:** Formulario validado con captura de imagen (Storage) y formateo estricto de precios (`%.2f €`).

---

## Arquitectura del Proyecto

### Lógica (Kotlin - `app/src/main/java/...`)

| Componente | Responsabilidad |
| :--- | :--- |
| **`FragmentPerfil`** | Orquesta la visibilidad de layouts (`layoutAdmin`/`layoutUsuario`) mediante escucha reactiva de Firebase. |
| **`RegisterActivity`** | Gestión de altas con triple campo: **Nombre de Usuario**, **Correo** y **Contraseña**. |
| **`GestionAdminsActivity`** | Panel del Jefe para generar invitaciones y listar administradores subordinados. |
| **`AdminAdapter`** | Adaptador para visualizar el equipo de gestión utilizando el campo `correo` de la Data Class. |
| **`Usuario.kt`** | Data Class actualizada con campos `nombre`, `correo`, `rol` y `esJefe`. |

### Diseño (XML - `app/src/main/res/layout/`)

* **`activity_main.xml`**: Login actualizado para acceso mediante **Correo Electrónico**.
* **`activity_register.xml`**: Formulario expandido con campo para **Nombre de Usuario** (identificador visual del perfil).
* **`activity_gestion_admins.xml`**: Interfaz con contenedor `CardView` en gris oscuro para el listado de equipo.
* **`item_admin.xml`**: Diseño de fila optimizado para mostrar el email del admin y botón de degradación.

---

## Stack Tecnológico

* **Lenguaje:** Kotlin 1.9+.
* **Servicios Cloud:** Firebase (Auth, Realtime Database, Storage).
* **Seguridad:** Reglas JSON configuradas para permitir lectura global de usuarios autenticados y escritura restringida al Jefe.
* **Librerías:** Material Design 3, Glide, ViewBinding.

---

## Notas de Versión
* **v1.4.0:** Refactorización de la base de datos: Clave `usuario` migrada a `correo` y adición del campo `nombre`.
* **v1.3.5:** Solucionado el conflicto de visibilidad solapada en el perfil mediante lógica de estado estricta.
* **v1.3.0:** Añadida jerarquía de **Admin Jefe** y sistema de códigos de invitación efímeros.
* **v1.2.5:** Rediseño del panel de gestión con contenedores en gris oscuro.
* **v1.1.0:** Refactorización de nomenclatura XML a estándares oficiales de Android.

---
© 2026 ArcadiaGames - Desarrollado para la gestión moderna de videojuegos.

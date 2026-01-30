# ArcadiaGames 🎮

**ArcadiaGames** es una aplicación Android nativa moderna diseñada para la gestión integral de una tienda de videojuegos. Destaca por su interfaz de usuario "Dark Gamer Premium", su navegación fluida y una integración robusta con los servicios de Firebase para una experiencia en tiempo real.

## Funcionalidades Principales

### 1. Autenticación y Seguridad por Roles
* **Seguridad:** Sistema de inicio de sesión y registro gestionado mediante **Firebase Authentication**.
* **Roles Dinámicos:** El sistema distingue automáticamente entre `admin` y `cliente` consultando **Firebase Realtime Database**.
    * **Clientes:** Acceso al catálogo, filtrado de juegos y gestión de perfil personal.
    * **Administradores:** Acceso exclusivo a herramientas de gestión, borrado masivo y edición de inventario.
* **Reglas de Acceso:** Implementación de reglas JSON en la base de datos para restringir la escritura en el nodo de productos únicamente a perfiles autorizados.

### 2. Experiencia de Usuario (UX/UI)
* **Navigation Rail:** Menú lateral ergonómico que maximiza el espacio vertical en pantallas móviles modernas.
* **Tema Oscuro:** Diseño visual de alta fidelidad con paleta de colores `#202020`, tarjetas con elevación y acentos vibrantes en verde neón y amarillo.
* **Transiciones Animadas:** Navegación suave y optimizada entre los diferentes fragmentos de la aplicación.

### 3. Pantalla de Inicio (Home)
* **Carrusel Infinito:** Implementación de `CarouselLayoutManager` con lógica de scroll circular para destacar las novedades de la tienda.
* **Datos Híbridos:** Sistema capaz de renderizar contenido promocional estático mientras sincroniza datos dinámicos desde la nube.

### 4. Catálogo Inteligente
* **Doble Filtrado:**
    * **Búsqueda:** Localización de títulos mediante `SearchBar` con filtrado en tiempo real.
    * **Chips de Plataforma:** Sistema de categorías (PC, PlayStation, Xbox, Nintendo) centrado dinámicamente mediante `HorizontalScrollView` optimizado.
* **Conectividad:** Sincronización en tiempo real mediante listeners de Firebase para reflejar cambios de stock o nuevos lanzamientos al instante.

### 5. Gestión de Inventario (Backend)
* **Editor Avanzado:** Actividad `AnadirProductoActivity` que permite la creación y modificación completa de fichas técnicas.
* **Gestión de Medios:** Captura de fotos mediante cámara frontal o selección de galería para subida directa a **Firebase Storage**.
* **Formateo de Precios:** Lógica de validación que asegura que los precios siempre se almacenen con dos decimales y el símbolo correspondiente (ej: `69.00 €`).

---

## Arquitectura del Proyecto

El proyecto sigue una arquitectura basada en Componentes de Android y vinculación de vistas mediante **ViewBinding**.

### 🛠 Lógica (Kotlin - `app/src/main/java/...`)

| Componente | Responsabilidad |
| :--- | :--- |
| **`MainActivity`** | Punto de entrada. Gestiona el flujo de autenticación y redirección por roles. |
| **`RegisterActivity`** | Gestión de altas de nuevos usuarios con vinculación de UID a Realtime Database. |
| **`HomeActivity`** | Contenedor principal que orquesta el `NavigationRail` y los fragmentos. |
| **`FragmentCatalogo`** | Interfaz principal de juegos con lógica de filtrado centrado y búsqueda. |
| **`AnadirProductoActivity`** | Formulario con validación de campos, captura de imagen y formateo de moneda. |
| **`GestionarInventarioActivity`** | Panel administrativo con lógica de borrado individual o masivo asíncrono. |
| **`FirebaseInventoryManager`** | Clase Singleton que centraliza las operaciones de red con RTDB y Storage. |

### Diseño (XML - `app/src/main/res/layout/`)

* **`activity_register.xml`**: Interfaz de registro optimizada (anteriormente `crearcuenta_layout.xml`).
* **`fragment_catalogo.xml`**: Layout con `HorizontalScrollView` y contenedores de gravedad para filtros centrados.
* **`item_juego_catalogo_u.xml`**: Diseño de tarjeta horizontal detallada para el listado de usuarios.
* **`activity_anadir_producto.xml`**: Formulario de edición con campos numéricos validados para decimales.

---

## Stack Tecnológico

* **Lenguaje:** Kotlin 1.9+.
* **Plataforma:** Android (minSdk 24).
* **Servicios Cloud:** Firebase Auth, Realtime Database y Storage.
* **Librerías:** Material Design 3, Glide (Carga de imágenes) y ViewBinding.

---

## 📝 Notas de Versión
* **v1.2.0:** Implementado el centrado dinámico de chips de plataforma en el catálogo para evitar alineaciones irregulares.
* **v1.1.5:** Integrada la lógica de formateo automático de precios `%.2f €` en la creación de productos.
* **v1.1.0:** Refactorización de nombres de archivos XML (`crearcuenta_layout` -> `activity_register`) para cumplir con los estándares de Android.
* **v1.0.5:** Añadida funcionalidad de borrado masivo sincronizado con contador de procesos asíncronos en el panel de gestión.
* **v1.0.0:** Lanzamiento inicial con sistema de roles y carrusel infinito.

---
© 2026 ArcadiaGames - Desarrollado para la gestión moderna de videojuegos.

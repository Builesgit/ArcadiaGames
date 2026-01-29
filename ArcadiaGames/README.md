# ArcadiaGames 🎮

**ArcadiaGames** es una aplicación Android nativa moderna para la gestión de una tienda de videojuegos ficticia. Destaca por su interfaz de usuario "Dark Gamer Premium", su navegación fluida y su integración robusta con Firebase.

## 🚀 Funcionalidades Principales

### 1. Autenticación y Roles
*   **Seguridad:** Inicio de sesión y registro mediante **Firebase Authentication**.
*   **Roles Dinámicos:** El sistema distingue automáticamente entre `admin` y `cliente` consultando **Firebase Realtime Database**.
    *   **Clientes:** Pueden ver el catálogo, filtrar juegos y ver su perfil.
    *   **Administradores:** Tienen acceso exclusivo al panel de gestión de inventario y botón flotante para añadir productos.

### 2. Experiencia de Usuario (UX/UI)
*   **Navigation Rail:** Menú lateral ergonómico que maximiza el espacio vertical en pantallas móviles modernas.
*   **Tema Oscuro:** Diseño visual cuidado con paleta de colores oscuros (`#202020`), tarjetas con elevación y acentos en verde neón y amarillo.
*   **Transiciones Animadas:** Navegación suave entre fragmentos.

### 3. Pantalla de Inicio (Home)
*   **Carrusel Infinito:** Implementación personalizada usando `CarouselLayoutManager` y lógica de scroll circular para destacar las novedades.
*   **Datos Híbridos:** Capaz de mostrar contenido promocional estático (recursos locales) mientras carga datos dinámicos.

### 4. Catálogo Inteligente
*   **Doble Filtrado:**
    *   **Búsqueda:** Filtra por nombre en tiempo real.
    *   **Chips de Plataforma:** Filtrado rápido por categorías (PC, PlayStation, Xbox, Nintendo).
*   **Conectividad:** Sincronización en tiempo real con el inventario de la nube.
*   **Gestión Rápida:** Botón flotante (FAB) integrado para que los administradores añadan juegos sin salir del catálogo.

### 5. Gestión de Inventario (Backend)
*   **Editor Potente:** Actividad dedicada (`AnadirProductoActivity`) para crear o modificar fichas de juegos.
*   **Gestión de Medios:** Subida de imágenes de portada directamente a **Firebase Storage**.
*   **Validación:** Formularios con validación de campos obligatorios, selectores (Spinners) personalizados y previsualización de imagen.

---

## 📂 Arquitectura del Proyecto

El proyecto sigue una arquitectura basada en Componentes de Android y MVVM simplificado.

### 🛠 Lógica (Kotlin - `app/src/main/java/...`)

| Componente | Responsabilidad |
| :--- | :--- |
| **`MainActivity`** | Control de acceso (Login) y enrutamiento por roles. |
| **`RegisterActivity`** | Registro de nuevos usuarios en Firebase Auth y DB. |
| **`HomeActivity`** | Contenedor principal. Gestiona el `NavigationRail` y la orquestación de fragmentos. |
| **`FragmentHome`** | Visualización de ofertas destacadas. Usa `JuegoAdapter` en modo Carrusel con datos locales. |
| **`FragmentCatalogo`** | Lista completa del inventario. Conecta con `FirebaseInventoryManager` para datos en tiempo real. |
| **`FragmentPerfil`** | Panel de usuario. Muestra opciones contextuales según si es Admin o Cliente. |
| **`AnadirProductoActivity`** | Actividad de formulario para Crear/Editar juegos y subir imágenes. |
| **`FirebaseInventoryManager`** | Clase Singleton que encapsula toda la lógica de red (Firebase RTDB + Storage). |
| **`JuegoAdapter`** | Adaptador flexible para RecyclerViews. Soporta modo Lista Vertical y Carrusel Horizontal. Maneja imágenes remotas (URL) y locales (Resource ID). |
| **`Juego.kt`** | Data Class híbrida con soporte para imágenes de internet (`imagenUrl`) y recursos locales (`imagenResId`). |

### 🎨 Diseño (XML - `app/src/main/res/layout/`)

*   **`activity_home.xml`**: Layout raíz con `CoordinatorLayout` y menú lateral ocultable.
*   **`fragment_catalogo.xml`**: Lista con `SearchBar`, `ChipGroup`, `RecyclerView` y FAB.
*   **`item_novedades_carrousel.xml`**: Tarjeta formato poster con degradado para el carrusel de inicio.
*   **`item_juego_catalogo_u.xml`**: Tarjeta detallada horizontal con etiquetas de colores para el listado del catálogo.
*   **`activity_anadir_producto.xml`**: Formulario de edición con estilo oscuro y campos validados.
*   **`activity_perfil.xml`**: Pantalla de perfil con menú de opciones estilizado.

---

## 🔧 Stack Tecnológico

*   **Lenguaje:** Kotlin 1.9+
*   **Plataforma:** Android (minSdk 24)
*   **Servicios en la Nube:**
    *   Firebase Authentication (Email/Password)
    *   Firebase Realtime Database (JSON NoSQL)
    *   Firebase Storage (Imágenes)
*   **Librerías Clave:**
    *   **Material Design 3:** Componentes UI modernos (NavigationRail, Carousel, Chips).
    *   **Glide:** Carga y caché de imágenes eficiente.
    *   **ViewBinding:** Vinculación segura de vistas XML.

## 📝 Notas de Configuración
Para compilar este proyecto correctamente:
1.  Asegúrate de tener el archivo `google-services.json` configurado en tu directorio `app/` conectado a tu proyecto Firebase.
2.  Las imágenes del carrusel de inicio requieren recursos en `res/drawable` (ej: `cyberpunk2077.jpg`, `wicher3.jpg`, etc.) para evitar errores de compilación si usas los datos de ejemplo del Home.

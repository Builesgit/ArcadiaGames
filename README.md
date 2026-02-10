# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa moderna diseñada para la gestión integral de una tienda de videojuegos. Destaca por su interfaz de usuario "Dark Gamer Premium", su navegación fluida y una integración robusta con los servicios de Firebase para una experiencia en tiempo real y segura.

---

## Funcionalidades Principales

### 1. Gestión de Roles y Seguridad
* **Autenticación:** Sistema gestionado mediante **Firebase Authentication**.
* **Seguridad por Roles:** Reglas de Firebase personalizadas que protegen nodos sensibles como `historial`, `usuarios` y `productos` según el nivel de acceso (Admin/Cliente).
* **Jerarquía de Usuarios:**
    * **Admin Jefe:** Capacidad exclusiva para gestionar el equipo y generar códigos de invitación.
    * **Administradores:** Acceso a inventario, gestión de incidencias y auditoría de movimientos.
    * **Clientes:** Acceso al catálogo, historial de compras y soporte técnico.

### 2. Registro de Actividad (Auditoría)
* **Historial Filtrado:** Sistema de pestañas (`TabLayout`) que permite a los administradores alternar entre logs de **Administradores** (altas/bajas de stock) y **Usuarios** (compras/actividad).
* **Logs en Tiempo Real:** Seguimiento automático de cada acción relevante en la app, almacenando usuario, tipo de acción, producto y marca de tiempo.
* **Interfaz Glassmorphism:** Tarjetas con diseño translúcido y tiempos relativos (ej: "hace 5 min") para una lectura rápida y estética.

### 3. Experiencia de Usuario (UX/UI Avanzada)
* **Perfil Dinámico:** Adaptación inmediata de la interfaz según el rol detectado en la base de datos.
* **Navigation Rail:** Menú lateral ergonómico para una navegación optimizada en pantallas modernas.
* **Tema Dark Premium:** Paleta `#202020` con acentos neón y optimización multimedia mediante **Glide**.

---

## Arquitectura del Proyecto

### Componentes de Auditoría (`v1.9.0`)

| Clase / Archivo | Responsabilidad |
| :--- | :--- |
| **`HistorialActivity`** | Gestiona el filtrado dinámico de logs mediante pestañas y Firebase. |
| **`HistorialAdapter`** | Vincula los datos de auditoría con la interfaz visual estilo "gamer". |
| **`AccionHistorial`** | Modelo de datos (POJO) para la trazabilidad de acciones. |
| **`item_historial.xml`** | Diseño de tarjeta personalizada para los registros del historial. |

---

## Stack Tecnológico

* **Lenguaje:** Kotlin 1.9+.
* **Plataforma:** Android (minSdk 27).
* **Servicios Cloud:** Firebase Auth, Realtime Database (con reglas de auditoría), Storage.
* **Librerías:** Glide 4.16, Material Design 3, ViewBinding.

---

## Notas de la Versión (v1.9.0)
* **Módulo de Auditoría Avanzada:** Implementación del Historial con filtros para diferenciar las acciones de gestión de las acciones de los clientes.
* **Seguridad de Datos:** Actualización de las reglas de Realtime Database para securizar el nodo `historial`, permitiendo lectura exclusiva a administradores.
* **Optimización de FirebaseInventoryManager:** Integración de disparadores automáticos de logs al añadir, editar o eliminar productos del inventario.
* **Rediseño Visual:** Implementación de tarjetas de historial con títulos dinámicos y cálculo de tiempo relativo.

---
© 2026 ArcadiaGames - Desarrollado para la gestión moderna de videojuegos.
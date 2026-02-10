# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa diseñada para la gestión premium de una tienda de videojuegos. Utiliza una arquitectura orientada a servicios de Firebase para ofrecer control total sobre el inventario, usuarios y auditoría en tiempo real.

---

## Funcionalidades del Dashboard Administrativo (v1.10.0)

El panel de control ha sido rediseñado para ofrecer una experiencia técnica y formal, optimizando el flujo de trabajo de los administradores.

### 1. Panel de Control Operativo
* **Acceso Rápido:** Tarjetas táctiles unificadas para las tareas de **Inventario** y **Añadir Producto**.
* **Estética Formal:** Diseño con bordes técnicos de `8dp` y fondos sobrios en azul profundo (`#1A2634`), eliminando distracciones visuales.
* **Jerarquía Visual:** Uso de títulos en **Amarillo Huevo** (`#FBC02D`) para una navegación rápida y profesional.

### 2. Gestión de Soporte y Tickets
* **Módulo de Incidencias:** Consola unificada para la revisión de reportes técnicos y errores enviados por los usuarios.
* **Iconografía Minimalista:** Todos los iconos han sido estandarizados en color blanco para mantener la sobriedad del entorno administrativo.

### 3. Consola de Historial (Monitor de Sistema)
* **Visualización Técnica:** Implementación del `historial_background`, un marco con borde de `3dp` en azul claro que resalta la actividad reciente del sistema.
* **Auditoría en Tiempo Real:** El historial general de la tienda se integra directamente en la pantalla de inicio, eliminando elementos de carga (ProgressBars) para una respuesta visual inmediata.

---

## Arquitectura de Interfaz

### Recursos de Diseño (`v1.10.0`)

| Recurso | Descripción |
| :--- | :--- |
| **`historial_background`** | Shape XML con fondo `#0A1324` y stroke técnico para la consola de monitoreo. |
| **`fragment_home.xml`** | Layout dinámico que alterna entre el catálogo para usuarios y el Dashboard para admins. |
| **`colors.xml`** | Paleta actualizada con acentos amarillo huevo y sombras azul oscuro. |

---

## Notas de la Versión (v1.10.0)
* **Dashboard Admin v2:** Evolución del menú de administración a un panel de control formal basado en tarjetas operativas.
* **Unificación Estética:** Sincronización de tonalidades entre los módulos de inventario, incidencias e historial.
* **Optimización de UX:** Reducción del radio de curvatura en componentes para un aspecto más "industrial/técnico".
* **Depuración Visual:** Limpieza de iconos de carga y colores secundarios (verdes) para centrar la atención en los datos críticos.

---
© 2026 ArcadiaGames - Consola de Administración Profesional.
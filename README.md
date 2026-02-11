# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa diseñada para la gestión premium de una tienda de videojuegos. Utiliza una arquitectura orientada a servicios de Firebase para ofrecer control total sobre el inventario, usuarios y auditoría en tiempo real.

---

## Funcionalidades del Dashboard Administrativo (v1.12.0)

El panel de control ha sido optimizado para ofrecer una experiencia técnica y formal, garantizando la integridad de los datos en cada operación.

### 1. Gestión Inteligente de Inventario y Stock
* **Control de Bajas Multinivel:** Implementación de un sistema de borrado lógico que diferencia entre la reducción de existencias y la eliminación de registros.
    * **Baja Unitaria:** Descuento rápido de una sola unidad mediante actualización de atributo.
    * **Baja Específica:** Diálogo con entrada numérica para retirar lotes concretos del inventario (ej: por rotura o devolución).
    * **Eliminación Total:** Purga completa del nodo en Firebase cuando el stock llega a cero o por decisión administrativa definitiva.
* **Integridad de Datos:** Bloqueo automático de campos críticos (Nombre, Categoría, Plataforma, Precio) durante la edición para evitar inconsistencias en la base de datos, permitiendo únicamente la modificación del flujo de stock.

### 2. Monitor de Auditoría y Trazabilidad (Consola de Historial)
* **Detección de Acciones Críticas:** El sistema ahora diferencia y etiqueta automáticamente las actividades para una supervisión precisa:
    * **`Nuevo Producto`**: Identificación estricta de altas iniciales en el catálogo.
    * **`Stock Actualizado`**: Registro detallado de variaciones numéricas (ej: "Admin redujo stock -5").
    * **`Producto Alquilado/Comprado`**: Monitorización en tiempo real de las transacciones de los usuarios.
* **Sistema de Tiempos Relativos:** Visualización técnica del tiempo transcurrido desde la última acción (ej: "ahora", "hace 15 min", "hace 2 d").
* **Filtrado por Tabs:** Consola dividida mediante `TabLayout` que separa la actividad administrativa de la actividad comercial.

### 3. Seguridad y Navegación Dinámica
* **Perfiles de Acceso:** Ocultación automática del módulo de "Cesta" para perfiles administrativos mediante la manipulación del `NavigationRail` en tiempo de ejecución.
* **Consola de Soporte:** Módulo unificado para la revisión de incidencias técnicas y reportes enviados por los usuarios finales.

---

## Arquitectura de Interfaz y Recursos

### Componentes de Datos (`v1.12.0`)

| Recurso | Descripción |
| :--- | :--- |
| **`EditarInventarioActivity`** | Clase especializada en la gestión de stock con validación de integridad y bloqueo de campos. |
| **`GestionarAdapter`** | Controlador de RecyclerView que soporta la lógica de borrado inteligente y visualización de stock agrupado. |
| **`DialogoDetalleJuegoBinding`** | Implementación de ViewBinding para pop-ups informativos, alquileres y compras. |
| **`historial_background`** | Shape XML con fondo `#0A1324` y stroke técnico para la consola de monitoreo. |

---

## Notas de la Versión (v1.12.0)
* **Gestión de Stock Avanzada:** Transición de un sistema de borrado de nodos a una actualización dinámica de atributos numéricos.
* **Refactorización de Historial:** Limpieza de etiquetas de acción para evitar mensajes genéricos ("Actividad") y mejorar la claridad de la auditoría.
* **Optimización de UX:** Implementación de diálogos de confirmación dinámicos con entradas numéricas para prevenir la pérdida accidental de datos.
* **Corrección de ViewBinding:** Unificación de layouts de diálogo (`dialogo_detalle_juego.xml`) para garantizar una compilación robusta.

---
© 2026 ArcadiaGames - Consola de Administración Profesional.
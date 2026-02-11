# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa diseñada para la gestión premium de una tienda de videojuegos. Utiliza una arquitectura orientada a servicios de Firebase para ofrecer control total sobre el inventario, usuarios y auditoría en tiempo real.

---

## Funcionalidades del Dashboard Administrativo (v1.14.0)

El panel de control ha sido optimizado para ofrecer una experiencia técnica y formal, garantizando la integridad de los datos y la automatización de procesos críticos.

### 1. Gestión Inteligente de Inventario y Stock
* **Control de Bajas Multinivel:** Sistema de borrado lógico que diferencia entre la reducción de existencias y la eliminación de registros.
    * **Baja Unitaria:** Descuento rápido de una unidad mediante actualización de atributo.
    * **Baja Específica:** Diálogo con entrada numérica para retirar lotes concretos del inventario.
    * **Eliminación Total:** Purga completa del nodo en Firebase al agotar stock o por decisión administrativa.
* **Integridad de Datos:** Bloqueo automático de campos críticos (Nombre, Categoría, Plataforma, Precio) durante la edición de stock para asegurar la consistencia del catálogo.

### 2. Monitor de Auditoría y Trazabilidad (Consola de Historial)
* **Detección de Acciones Críticas:** Identificación y etiquetado automático de actividades con mensajes personalizados:
    * **`Nuevo Producto`**: Registro de altas iniciales en el sistema.
    * **`Stock Actualizado`**: Trazabilidad de variaciones manuales de inventario.
    * **`Alquiler Realizado/Devuelto`**: Seguimiento específico del flujo de préstamos.
    * **`Compra Realizada`**: Registro de ventas finales procesadas.
* **Sistema de Tiempos Relativos:** Visualización técnica del tiempo transcurrido (ej: "ahora", "hace 15 min").
* **Filtrado por Tabs:** Separación lógica entre actividad administrativa (gestión) y comercial (usuarios).

### 3. Automatización y Ciclo de Vida de Alquileres
* **Gestión Transaccional de Stock:** Integración de procesos asíncronos que descuentan automáticamente el stock global solo al confirmar el pago o el alquiler, evitando registros prematuros en la cesta.
* **Retorno Automatizado de Inventario:** Motor de limpieza que detecta alquileres vencidos y reincorpora automáticamente las unidades al stock global, registrando el evento como "Alquiler devuelto" bajo el usuario "Sistema".

### 4. Seguridad y Navegación Dinámica
* **Perfiles de Acceso:** Ocultación dinámica del módulo de "Cesta" para perfiles administrativos mediante manipulación del `NavigationRail`.
* **Consola de Soporte:** Módulo centralizado para la revisión de incidencias técnicas reportadas por clientes.

---

## Arquitectura de Interfaz y Recursos

### Componentes de Datos (`v1.14.0`)

| Recurso | Descripción |
| :--- | :--- |
| **`EditarInventarioActivity`** | Gestión especializada de stock con validación de integridad y bloqueo de campos. |
| **`FirebaseInventoryManager`** | Motor de servicios para gestión de imágenes, productos y lógica de alquileres vencidos. |
| **`PagoActivity`** | Controlador de cierre de venta que procesa el descuento masivo de stock y limpieza de cesta. |
| **`HistorialAdapter`** | Controlador de RecyclerView con mapeo dinámico de acciones y estados de alquiler. |
| **`historial_background`** | Shape XML técnico para la consola de monitoreo en tiempo real. |

---

## Notas de la Versión (v1.14.0)
* **Sincronización Transaccional:** El stock ahora se descuenta estrictamente al finalizar el flujo de pago, no al añadir a la cesta.
* **Lógica de Alquileres Pro:** Diferenciación en el historial entre "Alquiler realizado" (usuario) y "Alquiler devuelto" (automatismo del sistema).
* **Refactorización de FirebaseManager:** Inclusión de procesos de recuperación de inventario en segundo plano.
* **Optimización de Auditoría:** Títulos dinámicos mejorados para una lectura rápida del flujo de caja y movimientos de almacén.

---
© 2026 ArcadiaGames - Consola de Administración Profesional.
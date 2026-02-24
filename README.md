# ArcadiaGames 

**ArcadiaGames** es una aplicación Android nativa diseñada para la gestión premium de una tienda de videojuegos. Utiliza una arquitectura orientada a servicios de Firebase para ofrecer control total sobre el inventario, usuarios y auditoría en tiempo real.

---

## Funcionalidades del Dashboard Administrativo 

El panel de control ha sido optimizado para ofrecer una experiencia técnica y formal, garantizando la integridad de los datos y la automatización de procesos críticos mediante una lógica puramente imperativa.

### 1. Nueva Arquitectura de Navegación (Overlay System)
* **Interfaz de Superposición:** Menú lateral basado en `NavigationRailView` que se despliega sobre el contenido mediante un sistema de capas (Z-index), evitando la deformación de la interfaz principal.
* **Cierre por Proximidad (Scrim):** Implementación de una capa de detección táctil (`view_scrim`) que permite cerrar el menú lateral al pulsar en cualquier área fuera de este.
* **Control de Acceso Dinámico:** Ocultación selectiva de módulos (como la "Cesta") en tiempo real según el rol del usuario (Admin/Cliente).

### 2. Gestión de Inventario y Cesta Transaccional
* **Migración a Fragmentos:** Integración de la "Cesta" como `FragmentCesta`, permitiendo una navegación fluida sin destruir el estado de la actividad principal.
* **Control de Stock Multinivel:** * **Baja Unitaria:** Descuento rápido mediante actualización de atributos en Firebase.
    * **Baja Específica:** Diálogo con entrada numérica para retirar lotes concretos.
* **Integridad Transaccional:** El stock se descuenta estrictamente al confirmar el pago en `PagoActivity`, garantizando que los productos en la cesta no bloqueen el inventario de otros usuarios.

### 3. Monitor de Auditoría y Trazabilidad
* **Detección de Acciones Críticas:** Identificación y etiquetado automático de actividades:
    * **`Nuevo Producto`**: Altas iniciales en el sistema.
    * **`Stock Actualizado`**: Trazabilidad de variaciones manuales.
    * **`Compra Realizada`**: Registro de ventas finales procesadas.
* **Motor de Tiempos Relativos:** Visualización técnica del tiempo transcurrido (ej: "ahora", "hace 15 min") mediante lógica de cálculo manual.

### 4. Optimización de Código y Rendimiento
* **Paradigma Imperativo:** Refactorización completa para eliminar programación funcional avanzada (`map`, `filter`, `sumOf`, `joinToString`). Se utilizan bucles `for` tradicionales y acumuladores manuales para asegurar la máxima compatibilidad y facilidad de depuración.
* **Gestión de Recursos asíncronos:** Uso de `FirebaseInventoryManager` para la subida de imágenes y sincronización de nodos en segundo plano.

---

## Arquitectura de Interfaz y Recursos

| Recurso | Descripción |
| :--- | :--- |
| **`HomeActivity`** | Host principal con soporte para navegación por superposición y gestión de Scrim. |
| **`FragmentCatalogo`** | Motor de búsqueda dinámico con filtrado multinivel por categorías y plataformas. |
| **`FragmentCesta`** | Módulo de gestión de compras integrado en el flujo de fragmentos principal. |
| **`AnadirProductoActivity`** | Interfaz de administración con validación de campos y gestión de medios vía Firebase Storage. |
| **`view_scrim`** | Componente de interfaz dedicado a la detección de toques fuera del área activa del menú. |

---

## Notas de la Versión 
* **Refactorización UI/UX:** El menú lateral ya no empuja el contenido; ahora flota elegantemente con un fondo traslúcido (`#CC000000`).
* **Corrección de Contraste:** Iconografía de navegación actualizada a blanco puro para mejorar la visibilidad sobre fondos oscuros.
* **Estabilidad:** Eliminación de lambdas complejas en procesos de cálculo de precios para evitar errores de precisión decimal.
* **Layouts Modernos:** Uso extendido de `ConstraintLayout` para garantizar que los títulos y botones no sean solapados por los elementos de navegación.

---
© 2026 ArcadiaGames - Consola de Administración Profesional.
# Arcadia Games - Sistema de Gestión de Inventario

**Proyecto**: ArcadiaGames

**Versión**: 1.0.0

**ArcadiaGames** es una aplicación Android nativa de alto rendimiento diseñada para la gestión premium de tiendas de videojuegos. El sistema centraliza el control de inventario, auditoría de transacciones y gestión de usuarios mediante una infraestructura basada en la nube con Firebase.

---

## Información del Proyecto (DAM2)

* **Proyecto:** Entrega Final - Proyecto Integrador 2025-2026.
* **Institución:** Universidad Europea Madrid.
* **Desarrollo:** Daniel Builes, Andrea Caballero, Alejandro Caloto, Adrían Garduño
* **Herramienta de Seguimiento:**
  * [JIRA] https://uedanibuiles.atlassian.net/jira/software/projects/SCRUM/boards/1/timeline
  * [Github] https://github.com/Kalrezor/Arcadia-Games-DAM-Integrador

---

## Estructura del Proyecto (Entrega Oficial)

Siguiendo los requisitos de la **Entrega Final**, el proyecto se organiza de la siguiente manera:

```text
Proyecto_ArcadiaGames/
├── CodigoFuente/           # Código nativo en Android Studio (Kotlin)
├── ManualUsuario/          # Documento PDF para el usuario final
├── Presentacion/          # Soporte visual para la defensa del proyecto
├── DocumentacionTecnica/   # Diagramas de clases, casos de uso y modelos de datos
├── Evidencias/             # Capturas de pantalla, diagramas y esquemas de BD
└── Enlaces.txt             # Resumen de accesos al repositorio y herramientas
```

---

## Contexto y Problema

En el mercado actual de los videojuegos, la gestión ágil del stock y la trazabilidad de las acciones administrativas son críticas. **ArcadiaGames** resuelve la fragmentación de datos mediante una arquitectura en tiempo real que permite a los administradores supervisar cada movimiento y a los clientes disfrutar de un catálogo siempre actualizado.

### Factores Clave:

* **Seguridad:** Autenticación robusta.
* **Escalabilidad:** Base de datos NoSQL con Firebase Realtime Database.
* **UX Premium:** Interfaz oscura (Dark Mode) con sistema de navegación por superposición.

---

## Funcionalidades Destacadas

### 1. Sistema de Navegación Overlay (Z-Index)

* **Menú Lateral Inteligente:** Utiliza un `NavigationRailView` que flota sobre el contenido sin desplazar la interfaz, optimizando el espacio visual.
* **Cierre por Scrim:** Capa táctil inteligente que detecta pulsaciones externas para minimizar el menú.

### 2. Gestión Transaccional del Inventario

* **Integridad de Stock:** El sistema garantiza que el stock solo se descuenta tras la confirmación real del pago en `PagoActivity`.
* **Control Multinivel:** Posibilidad de realizar bajas unitarias o por lotes específicos mediante diálogos dinámicos.

### 3. Monitor de Auditoría (Trazabilidad)

* Registro automático de acciones críticas: `Nuevo Producto`, `Stock Actualizado`, `Compra Realizada`.
* **Cálculo de Tiempos Relativos:** Algoritmo manual para mostrar la antigüedad de los registros (ej. "hace 5 min").

### 4. Geolocalización

* Integración con **Google Maps API** para la localización de puntos físicos de entrega o tiendas asociadas.

---

## Stack Tecnológico

| Tecnología                 | Uso                                      |
| :-------------------------- | :--------------------------------------- |
| **Kotlin**            | Lenguaje de programación principal      |
| **Firebase Auth**     | Gestión de identidades y acceso         |
| **Realtime Database** | Persistencia de datos en tiempo real     |
| **Firebase Storage**  | Almacenamiento de imágenes de productos |
| **Google Maps API**   | Servicios de geolocalización            |
| **Glide**             | Optimización de carga de imágenes      |
| **Material 3**        | Sistema de diseño y componentes         |

---

## Manual de Usuario (Resumen)

1. **Registro/Login:** Los usuarios deben autenticarse para acceder al catálogo y la cesta.
2. **Catálogo:** Navegación fluida por categorías y plataformas.
3. **Cesta:** Los productos se añaden temporalmente; el stock se descuenta solo al finalizar el pago.
4. **Panel Admin:** (Solo roles autorizados) Permite añadir, editar y eliminar productos, además de monitorizar la auditoría.

---

## Metodología y Planificación

Se ha seguido una metodología **Scrum** para el desarrollo:

* **Product Backlog:** Definido en la herramienta de gestión de tareas.
* **Sprints:** Divisiones semanales para implementación de módulos (Cesta, Auth, Mapas).
* **Evidencias:** El historial de commits refleja un desarrollo iterativo y colaborativo.

---

© 2026 ArcadiaGames - Universidad Europea Madrid. Propiedad del equipo de desarrollo DAM2.

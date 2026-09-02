# 📄 Global-Invoice — Microservicio Core (Java / Spring Boot)

Microservicio backend encargado de la gestión transaccional de facturas, aplicación del motor dinámico de tributación y la comunicación con el servicio público gubernamental SOAP para auditoría de importes.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 17+
* **Framework:** Spring Boot 3.x
* **Persistencia:** Spring Data JPA / PostgreSQL
* **Seguridad:** Spring Security + JWT
* **Gestor de dependencias:** Apache Maven
* **Contenedorización:** Docker / Docker Compose

---

## 💡 Funcionalidades Principales

* **RF-01 Motor Dinámico de Tributación:** Implementación del patrón de diseño **Strategy** para el cálculo dinámico de totales según el tipo de factura (Nacional, Exportación, Gubernamental) respetando los principios SOLID (Open/Closed)[cite: 1].
* **RF-03 Auditoría Legacy (SOAP):** Integración con el servicio WSDL público de *DataFlex (NumberConversion)* para la conversión del monto total a texto[cite: 1].
* **RF-05 Seguridad RBAC:** Protección de endpoints mediante tokens JWT con roles `OPERADOR` y `AUDITOR`[cite: 1].

---

## 🚀 Instalación y Ejecución Local

### Prerrequisitos
* Java JDK 17 o superior.
* Apache Maven.
* Docker Desktop y la red externa `global-invoice-net` activa (`docker network create global-invoice-net`).

### 1. Variables de Entorno (`.env`)
Crea un archivo `.env` en la raíz del proyecto basándote en la plantilla:
```env
PORT=8080
DB_HOST=postgres-db
DB_PORT=5432
DB_NAME=global_invoice
DB_USER=postgres
DB_PASSWORD=postgres_password
JWT_SECRET=super_secret_key_global_invoice_12345

### Ejecutar con Docker 

docker compose up --build


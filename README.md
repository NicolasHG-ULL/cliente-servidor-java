# Proyecto Cliente-Servidor en Java

Este proyecto es una implementación básica de una arquitectura cliente-servidor en Java, diseñada para estudiantes. El objetivo principal es mostrar cómo se puede estructurar un sistema modular en el que el servidor cargue servicios de manera dinámica desde archivos `.jar`, y los clientes se comuniquen con el servidor para utilizarlos.

---

## 🗂️ Estructura del Proyecto

El proyecto está organizado de la siguiente manera:
```
src
├── client
│   ├── Client.java
│   └── local-services
├── server
│   ├── Server.java
│   ├── ServiceAdapter.java
│   ├── Service.java
│   ├── ServiceLoader.java
│   └── services
│        └── GreetingService.jar
└── services
    ├── compile.sh
    ├── jar
    │   ├── ByeService.jar
    │   └── GreetingService.jar
    └── java
        ├── ByeService.java
        ├── GreetingService.java
        └── Service.java
```

## 📘 Descripción de las Carpetas

### `client`
La carpeta `client` contiene el código para el cliente.
- `Client.java`: Punto de entrada del cliente. Permite conectarse al servidor y utilizar los servicios disponibles.
- `local-services`: Esta carpeta está destinada a los archivos `.jar` de servicios que el cliente puede subir al servidor. 

### `server`
Incluye los archivos principales del servidor:
- `Server.java`: Maneja las conexiones con los clientes y ejecuta los servicios solicitados.
- `ServiceAdapter.java`: Permite que el servidor interactúe con los servicios cargados dinámicamente.
- `ServiceLoader.java`: Encargado de cargar los servicios desde los archivos `.jar` ubicados en la carpeta `services`.
- Carpeta `services`: Contiene los archivos `.jar` de los servicios que el servidor puede ofrecer.

### `services`
Esta carpeta está diseñada para facilitar el desarrollo de nuevos servicios. Contiene:
- `java`: Código fuente de los servicios en Java.
- `compile.sh`: Script para compilar y empaquetar los servicios en archivos `.jar`.
- `jar`: Carpeta donde se almacenan los servicios compilados en formato `.jar`.

> ❗❌ **Aviso Importante:**  
> Los servicios que se suben al servidor desde el cliente no incluyen comprobaciones de seguridad. Esto significa que cualquier archivo `.jar` puede ser cargado sin validación, lo que representa una mala práctica y una potencial vulnerabilidad de seguridad.  
> **Se recomienda no utilizar este enfoque en entornos reales o de producción.** Esta funcionalidad solo está destinada a fines educativos.


---

## 🛠️ Cómo Usar el Proyecto


### 1. Ejecutar el Servidor
1. Navega a la carpeta `src/server`.
2. Ejecuta el archivo `Server.java` para iniciar el servidor:
   ```bash
   java server.Server
   ```

### 2. Ejecutar el Cliente
1. Navega a la carpeta `src/client`.
2. Ejecuta el archivo `Client.java` para iniciar el cliente:
   ```bash
   java client.Client
   ```

### ⚠️ Consideraciones

- Si utilizas IntelliJ IDEA, configura el **Working Directory** de cada módulo:
    - Para el servidor: `src/server`
    - Para el cliente: `src/client`
- Si no utilizas IntelliJ, verifica que las rutas relativas a los archivos `.jar` sean correctas y ajústalas según el directorio desde el que ejecutes cada programa.


---

## 🚧 Requisitos para que un servicio funcione en el servidor

1. **Los servicios se cargan en el servidor como archivos `.jar`**  
   El archivo JAR debe contener los archivos .class de la clase del servicio y la interfaz.

2. **El nombre del servicio debe estar especificado en el archivo `MANIFEST.MF`**  
   El archivo JAR debe tener un archivo `META-INF/MANIFEST.MF` que incluya un atributo `Service-Class`, 
   que especifique el nombre completo de la clase del servicio (incluyendo su paquete).
   Ejemplo de `MANIFEST.MF`:
   ```
    Manifest-Version: 1.0
    Service-Class: services.java.GreetingService
   ```

3. **La clase debe tener un constructor público sin parámetros**  
   La clase debe tener un constructor público sin argumentos, para que pueda ser instanciada dinámicamente.

4. **Uso de un adaptador**
   El servidor usa un adaptador para convertir la interfaz del servicio a la interfaz que espera (server.Service).
   Si se desea cambiar la interfaz del servicio, es necesario ajustar el adaptador para que convierta las llamadas 
   a los métodos del nuevo servicio en la interfaz que espera el servidor.


### Limitaciones

1. Servicios simples de un solo archivo .java y una clase principal.
2. No se soportan dependencias externas fácilmente.
3. No se gestionan múltiples clases o archivos por servicio.
4. Limitación en el diseño del servicio al implementar la interfaz Service.

## 🔄 Automatización de la compilación y creación de JARs

Dentro de la carpeta `src/services` se ha incluido un script que automatiza el proceso de compilación de los servicios y generación de los correspondientes archivos JAR.

### Tareas del script:

* **Compilación:**
   * Busca todos los archivos `.java` dentro de `src/services/java`, excluyendo `Service.java`.
   * Compila los archivos encontrados, generando los archivos `.jar` en el directorio `services/jar`.
* **Generación de JARs:**
   * Para cada servicio, crea un archivo JAR que contiene:
      * Los archivos `.class` correspondientes al servicio.
      * Un archivo `MANIFEST.MF` que especifica la clase principal del servicio a través del atributo `Service-Class`.
* **Limpieza:**
   * Elimina los archivos temporales generados durante el proceso de compilación.

### Resumen

Este proceso garantiza que los servicios estén preparados para ser cargados dinámicamente por el servidor. 
La información contenida en el archivo `MANIFEST.MF` de cada JAR es crucial para que el servidor identifique y cargue correctamente cada servicio.

### ⚠️ Aviso

Es posible que al emplear el script haya que ajustar las rutas relativas de los directorios.
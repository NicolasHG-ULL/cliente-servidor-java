package server;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;

public class Server {

    // Ruta constante a la carpeta de servicios
    private static final String SERVICES_PATH = "services";

    // Lista de servicios cargados
    private final List<Service> services;

    // Servicio activo
    private Service activeService;

    /**
     * Constructor de la clase Server.
     * Inicializa la lista de servicios y establece el servicio activo como nulo.
     */
    public Server() {
        this.services = new ArrayList<>();
        this.activeService = null; // Al inicio no hay servicio activo
    }

    /**
     * Carga todos los servicios desde la carpeta con ruta SERVICES_PATH.
     * Los servicios deben estar empaquetados como archivos JAR y la carpeta debe existir.
     *
     * @throws Exception si ocurre un error al cargar un servicio.
     */
    private void loadServices() throws Exception {
        File servicesDir = new File(SERVICES_PATH); // Carpeta que contiene los JAR

        if (servicesDir.exists() && servicesDir.isDirectory()) {
            File[] jarFiles = servicesDir.listFiles((dir, name) -> name.endsWith(".jar"));

            if (jarFiles != null) {
                for (File jarFile : jarFiles) {
                    Service service = ServiceLoader.loadService(jarFile.getAbsolutePath());
                    services.add(service);
                    System.out.println("Servicio cargado: " + service.getName());
                }
            }
        } else {
            System.out.println("No se encontró la carpeta 'services' o está vacía.");
        }
    }

    /**
     * Establece el servicio activo usando el índice en la lista de servicios cargados.
     *
     * @param index Índice del servicio en la lista de servicios cargados.
     */
    public void setActiveService(int index) {
        if (index >= 0 && index < services.size()) {
            activeService = services.get(index);
            System.out.println("Servicio activo cambiado a: " + activeService.getName());
        } else {
            System.out.println("Índice inválido. No se cambió el servicio activo.");
        }
    }

    /**
     * Establece el servicio activo a nulo
     */
    public void clearActiveService() {
        activeService = null;
        System.out.println("El servicio activo ha sido desactivado. Actualmente no hay ningún servicio configurado.");
    }

    /**
     * Devuelve el servicio activo actual.
     *
     * @return El servicio activo o null si no hay servicio activo.
     */
    public Service getActiveService() {
        return activeService;
    }

    /**
     * Devuelve la lista de servicios.
     *
     * @return La lista con lo servicios disponibles en el servidor
     */
    public List<Service> getServices() {
        return services;
    }

    /**
     * Reinicia la lista de servicios y los recarga desde la carpeta 'services'.
     *
     * @throws Exception si ocurre un error al cargar un servicio.
     */
    public void reloadServices() throws Exception {
        services.clear();
        loadServices(); // Volver a cargar todos los servicios
    }

    /**
     * Devuelve una lista con los nombres de los servicios cargados.
     *
     * @return Lista de nombres de los servicios.
     */
    public List<String> getServiceNames() {
        List<String> names = new ArrayList<>();
        for (Service service : getServices()) {
            names.add(service.getName());
        }
        return names;
    }

    /**
     * Devuelve el nombre del servicio activo.
     *
     * @return Nombre del servicio activo o un mensaje si no hay servicio activo.
     */
    public String getActiveServiceName() {
        if (getActiveService() == null) {
            return "No hay ningún servicio activo.";
        }
        return getActiveService().getName();
    }

    /**
     * Devuelve el mensaje de ayuda del servicio activo.
     *
     * @return Mensaje de ayuda del servicio activo o un mensaje si no hay servicio activo.
     */
    public String getActiveServiceHelp() {
        if (getActiveService() == null) {
            return "No hay ningún servicio activo para mostrar la ayuda.";
        }
        return getActiveService().getHelp();
    }

    /**
     * Ejecuta el servicio activo con la entrada proporcionada.
     *
     * @param input Entrada que se pasará al servicio.
     * @return Resultado de la ejecución o un mensaje si no hay servicio activo.
     */
    public String executeActiveService(String input) {
        if (getActiveService() == null) {
            return "No hay ningún servicio activo para ejecutar.";
        }
        return getActiveService().execute(input);
    }

    /**
     * Inicia el servidor.
     * Carga los servicios desde la carpeta 'services' y muestra un mensaje de inicio.
     */
    public void start() {
        try {
            loadServices(); // Cargar servicios al iniciar el servidor
            System.out.println("Servidor iniciado.");
        } catch (Exception e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    /**
     * Método principal para iniciar el servidor.
     *
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        final int PORT = 12345; // Puerto para la comunicación
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado. Esperando conexión...");

            while (true) {  // Continuamos esperando conexiones indefinidamente
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream())) {

                    System.out.println("Cliente conectado.");
                    // Enviar un mensaje inicial
                    out.println("Conexión realizada. ¡Bienvenido al cliente de servicios!");

                    String command;
                    while ((command = in.readLine()) != null) {
                        System.out.println("Comando recibido: " + command);

                        switch (command) {
                            case "LIST_SERVICES":
                                // Enviar la lista de servicios disponibles al cliente
                                List<String> servicesNames = server.getServiceNames();
                                out.println(servicesNames.size());  // Primero el tamaño de la lista
                                for (String service : servicesNames) {
                                    out.println(service);  // Luego enviamos cada servicio
                                }
                                break;

                            case "ACTIVE_SERVICE":
                                // Recibimos el índice del servicio a activar
                                try {
                                    int serviceIndex = Integer.parseInt(in.readLine());
                                    server.setActiveService(serviceIndex);  // Llamar a la función para cambiar el servicio activo

                                    // Devolver el nombre del servicio activo o null si no se cambió
                                    if (server.getActiveService() != null) {
                                        out.println(server.getActiveServiceName());  // Servicio cambiado con éxito
                                    } else {
                                        out.println("null");  // Índice inválido, no se cambió el servicio
                                    }
                                } catch (NumberFormatException e) {
                                    out.println("null");  // Si el índice no es válido (no es un número)
                                }
                                break;

                            case "EXECUTE_SERVICE":
                                String input = in.readLine(); // Entrada para el servicio
                                System.out.println("Ejecutando servicio: " + server.getActiveServiceName());
                                out.println(server.executeActiveService(input));
                                break;

                            case "GET_INSTRUCTIONS":
                                out.println(server.getActiveServiceHelp());
                                break;

                            /*
                             * Nota: Los servicios subidos al servidor desde el cliente no incluyen validaciones de seguridad.
                             * Esto es inseguro y no debe usarse en entornos reales. Esta funcionalidad es solo para fines educativos.
                             */
                            case "UPLOAD_JAR":
                                // Leer el nombre del archivo .jar
                                String jarName = in.readLine();
                                System.out.println("Recibiendo archivo: " + jarName);

                                // Leer el tamaño del archivo
                                long fileSize = dataIn.readLong();
                                System.out.println("Tamaño del archivo: " + fileSize + " bytes");

                                // Crear un archivo para guardar el .jar en el servidor
                                try (FileOutputStream fos = new FileOutputStream(SERVICES_PATH + "/" + jarName)) {
                                    byte[] buffer = new byte[4096]; // Buffer para leer los datos
                                    long bytesRead = 0;
                                    while (bytesRead < fileSize) {
                                        int read = dataIn.read(buffer); // Leer el archivo en bloques
                                        if (read == -1) {
                                            break;
                                        }
                                        fos.write(buffer, 0, read); // Escribir los datos en el archivo
                                        bytesRead += read;
                                    }
                                    server.reloadServices();
                                    System.out.println("Archivo .jar recibido y almacenado: " + jarName);
                                    out.println("Servicio recibido con éxito: " + jarName); // Confirmar éxito al cliente
                                } catch (IOException e) {
                                    System.err.println("Error al recibir el archivo: " + e.getMessage());
                                    out.println("Error al recibir el archivo.");
                                } catch (Exception e) {
                                    out.println("Error al refrescar la lista de servicios");
                                    throw new RuntimeException(e);
                                }
                                break;

                            case "DEACTIVATE_SERVICE":
                                server.clearActiveService();
                                break;

                            case "RELOAD_SERVICES":
                                try {
                                    server.reloadServices();
                                    out.println("La lista de servicios se ha recargado con éxito");
                                } catch (Exception e) {
                                    out.println("Error al refrescar la lista de servicios");
                                    throw new RuntimeException(e);
                                }
                                break;

                            case "EXIT":
                                System.out.println("Cliente desconectado.");
                                break;

                            default:
                                out.println("Comando no reconocido.");
                        }
                    }

                } catch (IOException e) {
                    System.out.println("Error con el cliente. Continuando...");
                    server.clearActiveService();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

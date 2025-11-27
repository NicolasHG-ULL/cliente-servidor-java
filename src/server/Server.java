package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final String SERVICES_PATH = "services";
    private final List<Service> services;
    private Service activeService;

    public Server() {
        this.services = new ArrayList<>();
        this.activeService = null;
    }

    private void loadServices() throws Exception {
        File servicesDir = new File(SERVICES_PATH);
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

    public void setActiveService(int index) {
        if (index >= 0 && index < services.size()) {
            activeService = services.get(index);
            System.out.println("Servicio activo cambiado a: " + activeService.getName());
        } else {
            System.out.println("Índice inválido. No se cambió el servicio activo.");
        }
    }

    public void clearActiveService() {
        activeService = null;
        System.out.println("Servicio desactivado.");
    }

    public Service getActiveService() {
        return activeService;
    }

    public void reloadServices() throws Exception {
        services.clear();
        loadServices();
    }

    public List<String> getServiceNames() {
        List<String> names = new ArrayList<>();
        for (Service s : services) {
            names.add(s.getName());
        }
        return names;
    }

    public String getActiveServiceName() {
        return getActiveService() == null ? "No hay ningún servicio activo." : getActiveService().getName();
    }

    public String getActiveServiceHelp() {
        return getActiveService() == null ? "No hay ningún servicio activo para mostrar la ayuda."
                : getActiveService().getHelp();
    }

    public String executeActiveService(String input) {
        return getActiveService() == null ? "No hay ningún servicio activo para ejecutar."
                : getActiveService().execute(input);
    }

    public void start() {
        try {
            loadServices();
            System.out.println("Servidor iniciado.");
        } catch (Exception e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        final int PORT = 12345;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor escuchando en el puerto " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado.");

                new Thread(() -> handleClient(server, clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Server server, Socket clientSocket) {
        try (clientSocket; DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream())) {
            try {

                // Saludo inicial
                dataOut.writeUTF("Conexión realizada. ¡Bienvenido al cliente de servicios!");
                dataOut.flush();

                boolean running = true;
                while (running) {
                    String command = dataIn.readUTF(); // leer comando
                    System.out.println("Comando recibido: " + command);

                    switch (command) {
                        case "LIST_SERVICES":
                            List<String> names = server.getServiceNames();
                            dataOut.writeInt(names.size());
                            for (String n : names) {
                                dataOut.writeUTF(n);
                            }
                            dataOut.flush();
                            break;

                        case "ACTIVE_SERVICE":
                            int idx = dataIn.readInt();
                            server.setActiveService(idx);
                            dataOut.writeUTF(server.getActiveService() != null ? server.getActiveServiceName() : "null");
                            dataOut.flush();
                            break;

                        case "EXECUTE_SERVICE":
                            String input = dataIn.readUTF();
                            dataOut.writeUTF(server.executeActiveService(input));
                            dataOut.flush();
                            break;

                        case "GET_INSTRUCTIONS":
                            dataOut.writeUTF(server.getActiveServiceHelp());
                            dataOut.flush();
                            break;

                        case "UPLOAD_JAR":
                            // Leer nombre y tamaño
                            String jarName = dataIn.readUTF();
                            long fileSize = dataIn.readLong();
                            System.out.println("Recibiendo archivo: " + jarName + " (" + fileSize + " bytes)");

                            File outFile = new File(SERVICES_PATH + "/" + jarName);
                            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                                byte[] buffer = new byte[4096];
                                long totalRead = 0;
                                while (totalRead < fileSize) {
                                    int read = dataIn.read(buffer);
                                    if (read == -1) break;
                                    fos.write(buffer, 0, read);
                                    totalRead += read;
                                }
                                fos.flush();
                            }

                            server.reloadServices();
                            System.out.println("Archivo .jar recibido: " + jarName);
                            dataOut.writeUTF("Servicio recibido con éxito: " + jarName);
                            dataOut.flush();
                            break;

                        case "DEACTIVATE_SERVICE":
                            server.clearActiveService();
                            break;

                        case "RELOAD_SERVICES":
                            server.reloadServices();
                            dataOut.writeUTF("Lista de servicios recargada con éxito.");
                            dataOut.flush();
                            break;

                        case "EXIT":
                            System.out.println("Cliente desconectado.");
                            running = false;
                            break;

                        default:
                            dataOut.writeUTF("Comando no reconocido.");
                            dataOut.flush();
                            break;
                    }
                }

            } catch (IOException | RuntimeException e) {
                System.out.println("Error con el cliente: " + e.getMessage());
                server.clearActiveService();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException ignored) {
        }
    }
}

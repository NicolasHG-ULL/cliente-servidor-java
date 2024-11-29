package client;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String LOCAL_SERVICES_PATH = "local_services";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int mainMenuOption;

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             Scanner in = new Scanner(socket.getInputStream());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

            // Leer la bienvenida del servidor
            System.out.println(in.nextLine());

            do {
                System.out.println("\nSeleccione la opción que quiera ejecutar:");
                System.out.println("1. Mostrar servicios disponibles");
                System.out.println("2. Ejecutar un servicio");
                System.out.println("3. Subir un nuevo servicio");
                System.out.println("4. Refrescar lista de servicios");
                System.out.println("5. Salir");
                System.out.print("Opción: ");
                mainMenuOption = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea

                switch (mainMenuOption) {
                    case 1:
                        // Solicitar y mostrar los servicios disponibles al servidor
                        showServicesFromServer(out, in);
                        break;

                    case 2:
                        System.out.println("Seleccione el servicio que desea ejecutar:");
                        // Aquí se piden los servicios disponibles primero
                        showServicesFromServer(out, in);

                        System.out.print("Índice del servicio: ");
                        int activeServiceIndex = scanner.nextInt();
                        scanner.nextLine();

                        out.println("ACTIVE_SERVICE");  // Enviar solicitud de activar servicio
                        out.println(activeServiceIndex-1);  // Enviar el índice del servicio
                        out.flush();  // Asegurar que los datos se envíen

                        // Leer la respuesta del servidor
                        String serverResponse = in.nextLine();
                        if (serverResponse == null) {
                            System.out.println("Índice no válido. Volviendo al menú principal.");
                            break;
                        }

                        System.out.println("\nEstá ejecutando el servicio: " + serverResponse);
                        serviceMenu(scanner, serverResponse, out, in);
                        break;

                    case 3:
                        uploadJarFile(scanner, out, in, dataOut);
                        break;

                    case 4:
                        out.println("RELOAD_SERVICES");  // Enviar solicitud de activar servicio
                        out.flush();
                        // Leer la respuesta del servidor
                        String serverStatus = in.nextLine();
                        System.out.println("\n" + serverStatus);
                        break;

                    case 5:
                        System.out.println("Saliendo del cliente. ¡Hasta luego!");
                        break;

                    default:
                        System.out.println("Opción no válida. Por favor, intente de nuevo.");
                }
            } while (mainMenuOption != 5);

        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
        }

        scanner.close();
    }

    private static void showServicesFromServer(PrintWriter out, Scanner in) {
        // Solicitar la lista de servicios
        out.println("LIST_SERVICES");

        int serviceCount = Integer.parseInt(in.nextLine());  // Recibimos el número de servicios
        System.out.println("\nServicios disponibles:");
        for (int i = 1; i <= serviceCount; i++) {
            String serviceName = in.nextLine();  // Leemos cada nombre de servicio
            System.out.println(i + ". " + serviceName);  // Mostrar los servicios numerados
        }

    }

    private static void serviceMenu(Scanner scanner, String serviceName, PrintWriter out, Scanner in) {
        int serviceMenuOption;

        do {
            System.out.println("\nMenú del servicio activo: " + serviceName);
            System.out.println("1. Mostrar instrucciones");
            System.out.println("2. Enviar entrada");
            System.out.println("3. Salir");
            System.out.print("Opción: ");
            serviceMenuOption = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea

            switch (serviceMenuOption) {
                case 1:
                    // Aquí se pedirían las instrucciones del servicio al servidor
                    out.println("GET_INSTRUCTIONS");
                    out.flush();
                    String instructions = in.nextLine(); // Leer las instrucciones enviadas por el servidor
                    System.out.println("Instrucciones del servicio activo: " + instructions);
                    break;

                case 2:
                    System.out.print("Ingrese una cadena para enviar al servicio: ");
                    String input = scanner.nextLine();
                    out.println("EXECUTE_SERVICE");
                    out.println(input);
                    out.flush();
                    String response = in.nextLine(); // Leer la respuesta del servicio
                    System.out.println("\nRespuesta del servicio: " + response);
                    break;

                case 3:
                    System.out.println("Saliendo del servicio activo...");
                    out.println("DEACTIVATE_SERVICE");  // Enviar solicitud de desactivar servicio
                    out.flush();
                    break;

                default:
                    System.out.println("Opción no válida. Por favor, intente de nuevo.");
            }
        } while (serviceMenuOption != 3);
    }

    private static void uploadJarFile(Scanner scanner, PrintWriter out, Scanner in, DataOutputStream dataOut) {
        // Ruta de la carpeta local de los servicios
        File folder = new File(LOCAL_SERVICES_PATH);
        File[] jarFiles = folder.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jarFiles != null && jarFiles.length > 0) {
            System.out.println("\nSeleccione el archivo .jar que desea subir:");

            // Mostrar los archivos .jar encontrados en la carpeta
            for (int i = 0; i < jarFiles.length; i++) {
                System.out.println((i + 1) + ". " + jarFiles[i].getName());
            }

            // Solicitar al usuario que seleccione un archivo
            System.out.print("Índice del archivo: ");
            int selectedIndex = scanner.nextInt();
            scanner.nextLine();  // Consumir el salto de línea

            if (selectedIndex < 1 || selectedIndex > jarFiles.length) {
                System.out.println("Selección no válida. Volviendo al menú principal.");
                return;
            }

            File selectedFile = jarFiles[selectedIndex - 1];
            System.out.println("\nSeleccionado: " + selectedFile.getName());

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(selectedFile))) {
                // Enviar el nombre del archivo al servidor
                out.println("UPLOAD_JAR");
                out.println(selectedFile.getName());
                out.flush();

                // Enviar el tamaño del archivo como long
                long fileSize = selectedFile.length();
                dataOut.writeLong(fileSize);  // Escribir el tamaño del archivo como long
                System.out.println("Tamaño del archivo enviado: " + fileSize);
                dataOut.flush();

                // Enviar el archivo al servidor
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    dataOut.write(buffer, 0, bytesRead);
                }
                dataOut.flush();

                // Leer la respuesta del servidor (si la hay)
                String response = in.nextLine();
                System.out.println(response);

            } catch (IOException e) {
                System.err.println("Error al subir el archivo .jar: " + e.getMessage());
            }
        } else {
            System.out.println("No se encontraron archivos .jar en la carpeta 'local-services'.");
        }
    }
}
